package com.dungeoncrawler.wearos.domain.usecase

import com.dungeoncrawler.wearos.domain.catalog.DungeonCatalog
import com.dungeoncrawler.wearos.domain.model.CombatAction
import com.dungeoncrawler.wearos.domain.model.CombatOutcome
import com.dungeoncrawler.wearos.domain.model.GameState
import com.dungeoncrawler.wearos.domain.model.HeroPower
import com.dungeoncrawler.wearos.domain.model.ItemPassive
import com.dungeoncrawler.wearos.domain.model.Monster
import com.dungeoncrawler.wearos.domain.model.MonsterRole
import com.dungeoncrawler.wearos.domain.repository.GameProgressRepository
import com.dungeoncrawler.wearos.domain.repository.HeroRepository
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Resolves one player turn against the floor's boss. Every number here comes from equipped gear
 * — crit chance, armor pierce, life steal and riposte are all item passives, never level-ups.
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

        heroRepository.updateHeroStats { it.copy(currentHp = turn.heroHp) }

        val finalOutcome = when {
            turn.monsterHp <= 0 -> CombatOutcome.MonsterSlain(monster, rollLoot(monster))
            turn.heroHp <= 0 -> CombatOutcome.PlayerDefeated(monster)
            else -> turn.outcome
        }

        publishState(finalOutcome, monster, turn)
        return finalOutcome
    }

    // ------------------------------------------------------------------ actions

    private fun attack(power: HeroPower, monster: Monster, monsterHp: Int): TurnResult {
        val isCritical = random.nextInt(100) < power.total.critRate
        val raw = power.total.attack.let { if (isCritical) (it * 1.8f).roundToInt() else it }
        val damage = raw.afterMonsterDefense(monster, power)

        val lifeStolen = (damage * power.passiveMagnitude(ItemPassive.LIFE_STEAL)).roundToInt()
        val heroHp = (power.currentHp + lifeStolen).coerceAtMost(power.maxHp)

        return TurnResult(
            monsterHp = monsterHp - damage,
            heroHp = heroHp,
            outcome = if (isCritical) {
                CombatOutcome.PlayerCriticalHit(damage, lifeStolen)
            } else {
                CombatOutcome.PlayerStandardHit(damage, lifeStolen)
            },
        )
    }

    private fun spell(power: HeroPower, monsterHp: Int): TurnResult {
        // Spells bypass armor entirely — that is what makes magic gear worth a slot.
        val damage = (power.total.magicPower + random.nextInt(0, 6)).coerceAtLeast(1)
        return TurnResult(
            monsterHp = monsterHp - damage,
            heroHp = power.currentHp,
            outcome = CombatOutcome.PlayerSpellCast(damage),
        )
    }

    private fun defend(power: HeroPower, monster: Monster, monsterHp: Int): TurnResult {
        val incoming = monster.attack
        val parried = random.nextInt(100) < PARRY_CHANCE_PERCENT

        if (parried) {
            val riposte = (incoming * power.passiveMagnitude(ItemPassive.RIPOSTE)).roundToInt()
            return TurnResult(
                monsterHp = monsterHp - riposte,
                heroHp = power.currentHp,
                outcome = CombatOutcome.PlayerParried(incoming, riposte),
            )
        }

        val mitigated = ((incoming - power.total.defense) * (1f - power.total.damageReduction / 100f))
            .roundToInt()
            .coerceAtLeast(1)

        return TurnResult(
            monsterHp = monsterHp,
            heroHp = power.currentHp - mitigated,
            outcome = CombatOutcome.PlayerDamaged(mitigated),
        )
    }

    private fun Int.afterMonsterDefense(monster: Monster, power: HeroPower): Int {
        val pierced = (monster.defense * (1f - power.passiveMagnitude(ItemPassive.ARMOR_PIERCE)))
        return (this - pierced).roundToInt().coerceAtLeast(1)
    }

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
    }
}
