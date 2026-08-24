package com.dungeoncrawler.wearos.domain.usecase

import com.dungeoncrawler.wearos.domain.catalog.DungeonCatalog
import com.dungeoncrawler.wearos.domain.model.CombatAction
import com.dungeoncrawler.wearos.domain.model.CombatOutcome
import com.dungeoncrawler.wearos.domain.model.GameState
import com.dungeoncrawler.wearos.domain.model.HeroPower
import com.dungeoncrawler.wearos.domain.model.ItemPassive
import com.dungeoncrawler.wearos.domain.model.Monster
import com.dungeoncrawler.wearos.domain.model.MonsterRole
import com.dungeoncrawler.wearos.domain.model.StatBlock
import com.dungeoncrawler.wearos.domain.repository.GameProgressRepository
import com.dungeoncrawler.wearos.domain.repository.HeroRepository
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Resolves one player turn against the floor's boss. Every number here comes from equipped gear
 * — crit chance and damage, armor pierce, life steal, dodge and thorns are all item stats or
 * passives, never level-ups.
 */
class ExecuteCombatActionUseCase @Inject constructor(
    private val heroRepository: HeroRepository,
    private val gameProgressRepository: GameProgressRepository,
    private val computeHeroPower: ComputeHeroPowerUseCase,
    private val advanceFloor: AdvanceFloorUseCase,
    private val rollLoot: RollLootUseCase,
) {
    private val random = Random.Default

    suspend operator fun invoke(
        action: CombatAction,
        monster: Monster,
        monsterHp: Int,
    ): CombatOutcome {
        val power = computeHeroPower.once()

        val turn = when (action) {
            CombatAction.ATTACK -> attack(power, monster, monsterHp)
            CombatAction.SPELL -> spell(power, monsterHp)
            CombatAction.DEFENSE -> defend(power, monster, monsterHp)
        }

        // Regen ticks once per turn, after the exchange resolves, and never past the ceiling.
        val heroHp = (turn.heroHp + power.total.hpRegen).coerceAtMost(power.maxHp)
        heroRepository.updateHeroStats { it.copy(currentHp = heroHp) }

        val finalOutcome = when {
            turn.monsterHp <= 0 -> CombatOutcome.MonsterSlain(monster, rollLoot(monster))
            heroHp <= 0 -> CombatOutcome.PlayerDefeated(monster)
            else -> turn.outcome
        }

        publishState(finalOutcome, monster, turn.copy(heroHp = heroHp))
        return finalOutcome
    }

    // ------------------------------------------------------------------ actions

    private fun attack(power: HeroPower, monster: Monster, monsterHp: Int): TurnResult {
        val isCritical = random.nextInt(100) < power.total.critRate
        val critMultiplier = StatBlock.BASE_CRIT_MULTIPLIER + power.total.critDamage / 100f

        val raw = power.total.attack.let { if (isCritical) (it * critMultiplier).roundToInt() else it }
        val damage = raw.afterMonsterDefense(monster, power)

        val healed = (damage * power.lifeStealShare()).roundToInt()
        val heroHp = (power.currentHp + healed).coerceAtMost(power.maxHp)

        return TurnResult(
            monsterHp = monsterHp - damage,
            heroHp = heroHp,
            outcome = if (isCritical) {
                CombatOutcome.PlayerCriticalHit(damage, healed)
            } else {
                CombatOutcome.PlayerStandardHit(damage, healed)
            },
        )
    }

    private fun spell(power: HeroPower, monsterHp: Int): TurnResult {
        // Spells bypass armor entirely — that is what makes magic gear worth a slot.
        val damage = (power.total.magicPower + random.nextInt(0, 6)).coerceAtLeast(1)
        val healed = (damage * power.lifeStealShare()).roundToInt()

        return TurnResult(
            monsterHp = monsterHp - damage,
            heroHp = (power.currentHp + healed).coerceAtMost(power.maxHp),
            outcome = CombatOutcome.PlayerSpellCast(damage),
        )
    }

    private fun defend(power: HeroPower, monster: Monster, monsterHp: Int): TurnResult {
        val incoming = monster.attack

        // Dodge is checked before the parry roll: avoiding a hit outright beats blunting it.
        if (random.nextInt(100) < power.total.dodge) {
            return TurnResult(
                monsterHp = monsterHp,
                heroHp = power.currentHp,
                outcome = CombatOutcome.PlayerDodged(incoming),
            )
        }

        if (random.nextInt(100) < PARRY_CHANCE_PERCENT) {
            val riposte = (incoming * power.passiveMagnitude(ItemPassive.RIPOSTE)).roundToInt()
            return TurnResult(
                monsterHp = monsterHp - riposte,
                heroHp = power.currentHp,
                outcome = CombatOutcome.PlayerParried(incoming, riposte),
            )
        }

        val mitigated = ((incoming - power.total.defense) * (1f - power.damageReductionShare()))
            .roundToInt()
            .coerceAtLeast(1)
        val reflected = (mitigated * power.total.thorns / 100f).roundToInt()

        return TurnResult(
            monsterHp = monsterHp - reflected,
            heroHp = power.currentHp - mitigated,
            outcome = CombatOutcome.PlayerDamaged(mitigated, reflected),
        )
    }

    private fun Int.afterMonsterDefense(monster: Monster, power: HeroPower): Int {
        // The armor-pierce stat and the legendary passive stack, capped so defense still counts.
        val pierceShare = (power.total.armorPierce / 100f +
            power.passiveMagnitude(ItemPassive.ARMOR_PIERCE)).coerceAtMost(MAX_ARMOR_PIERCE)
        val effectiveDefense = monster.defense * (1f - pierceShare)
        return (this - effectiveDefense).roundToInt().coerceAtLeast(1)
    }

    /** Life-steal stat plus the legendary passive, capped so a full heal per hit is impossible. */
    private fun HeroPower.lifeStealShare(): Float =
        (total.lifeSteal / 100f + passiveMagnitude(ItemPassive.LIFE_STEAL))
            .coerceAtMost(MAX_LIFE_STEAL)

    private fun HeroPower.damageReductionShare(): Float =
        (total.damageReduction / 100f + passiveMagnitude(ItemPassive.DAMAGE_REDUCTION))
            .coerceAtMost(MAX_DAMAGE_REDUCTION)

    // ------------------------------------------------------------------ state

    private suspend fun publishState(outcome: CombatOutcome, monster: Monster, turn: TurnResult) {
        val progress = heroRepository.getDungeonProgress()

        val nextState = when (outcome) {
            is CombatOutcome.MonsterSlain -> when (monster.role) {
                MonsterRole.SUPREME_BOSS -> {
                    val next = advanceFloor.markCleared()
                    GameState.DungeonCleared(
                        dungeon = DungeonCatalog.findById(progress.currentDungeonId) ?: DungeonCatalog.FIRST,
                        supremeBoss = monster,
                        loot = outcome.loot,
                        nextDungeon = next,
                    )
                }
                else -> {
                    advanceFloor.advance()
                    GameState.FloorCleared(
                        progress = heroRepository.getDungeonProgress(),
                        monster = monster,
                        loot = outcome.loot,
                    )
                }
            }
            is CombatOutcome.PlayerDefeated -> GameState.Defeat(progress, monster)
            else -> GameState.InCombat(
                progress = progress,
                monster = monster,
                monsterHp = turn.monsterHp.coerceAtLeast(0),
                heroHp = turn.heroHp.coerceAtLeast(0),
                lastOutcome = outcome,
            )
        }

        gameProgressRepository.setGameState(nextState)
    }

    private data class TurnResult(
        val monsterHp: Int,
        val heroHp: Int,
        val outcome: CombatOutcome,
    )

    private companion object {
        const val PARRY_CHANCE_PERCENT = 40

        // Ceilings keep a fully-geared hero strong without making them unkillable.
        const val MAX_ARMOR_PIERCE = 0.75f
        const val MAX_LIFE_STEAL = 0.50f
        const val MAX_DAMAGE_REDUCTION = 0.60f
    }
}
