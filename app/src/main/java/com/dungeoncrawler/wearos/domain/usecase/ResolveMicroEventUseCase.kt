package com.dungeoncrawler.wearos.domain.usecase

import com.dungeoncrawler.wearos.core.haptics.HapticFeedbackManager
import com.dungeoncrawler.wearos.core.haptics.HapticPattern
import com.dungeoncrawler.wearos.domain.model.GameState
import com.dungeoncrawler.wearos.domain.model.HeroPower
import com.dungeoncrawler.wearos.domain.model.MicroEvent
import com.dungeoncrawler.wearos.domain.repository.GameProgressRepository
import com.dungeoncrawler.wearos.domain.repository.HeroRepository
import com.dungeoncrawler.wearos.domain.repository.MonsterRepository
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Resolves one passive 200-step event against the current dungeon's micro-mob pool: loot, a
 * trap, or an off-screen skirmish. Nothing here needs the player to look at the watch.
 */
class ResolveMicroEventUseCase @Inject constructor(
    private val heroRepository: HeroRepository,
    private val monsterRepository: MonsterRepository,
    private val gameProgressRepository: GameProgressRepository,
    private val computeHeroPower: ComputeHeroPowerUseCase,
    private val rollLoot: RollLootUseCase,
    private val hapticFeedbackManager: HapticFeedbackManager,
) {
    private val random = Random.Default

    suspend operator fun invoke(): MicroEvent? {
        val progress = heroRepository.getDungeonProgress()
        val power = computeHeroPower.once()

        val event = when (random.nextInt(10)) {
            in 0..2 -> resolveLoot(progress.currentDungeonId)
            in 3..4 -> resolveTrap(power)
            else -> resolveMicroMob(progress.currentDungeonId, power)
        } ?: return null

        // Regen ticks once per resolved event, so it pays out while simply walking.
        if (power.total.hpRegen > 0) {
            heroRepository.updateHeroStats { hero ->
                hero.copy(currentHp = (hero.currentHp + power.total.hpRegen).coerceAtMost(power.maxHp))
            }
        }

        gameProgressRepository.setGameState(GameState.MicroEventResolved(progress, event))
        hapticFeedbackManager.play(event.hapticPattern())
        return event
    }

    private suspend fun resolveLoot(dungeonId: String): MicroEvent? {
        val mob = monsterRepository.randomMicroMob(dungeonId) ?: return null
        val item = rollLoot(mob) ?: return null
        return MicroEvent.Loot(item)
    }

    private suspend fun resolveTrap(power: HeroPower): MicroEvent =
        MicroEvent.Trap(damage = applyDamage(random.nextInt(6, 16), power))

    private suspend fun resolveMicroMob(dungeonId: String, power: HeroPower): MicroEvent? {
        val mob = monsterRepository.randomMicroMob(dungeonId) ?: return null
        val raw = (mob.attack * random.nextDouble(0.4, 0.9)).roundToInt()
        return MicroEvent.MicroMobSlain(
            monster = mob,
            damageTaken = applyDamage(raw, power),
            loot = rollLoot(mob),
        )
    }

    /**
     * Shared mitigation path so defensive gear matters while walking too: dodge can void the hit
     * outright, then defense and damage reduction blunt what lands.
     *
     * A passive event never kills — HP floors at 1. Losing a run without looking at the watch
     * would punish the player for the mode the game is built around.
     */
    private suspend fun applyDamage(raw: Int, power: HeroPower): Int {
        if (random.nextInt(100) < power.total.dodge) return 0

        val mitigated = ((raw - power.total.defense / 2) * (1f - power.total.damageReduction / 100f))
            .roundToInt()
            .coerceAtLeast(1)
        heroRepository.updateHeroStats { hero ->
            hero.copy(currentHp = (hero.currentHp - mitigated).coerceAtLeast(1))
        }
        return mitigated
    }

    private fun MicroEvent.hapticPattern(): HapticPattern = when (this) {
        is MicroEvent.Loot -> HapticPattern.MICRO_EVENT_LOOT
        is MicroEvent.Trap -> HapticPattern.MICRO_EVENT_TRAP
        is MicroEvent.MicroMobSlain ->
            if (loot != null) HapticPattern.MICRO_EVENT_LOOT else HapticPattern.STANDARD_HIT
    }
}
