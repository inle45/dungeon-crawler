package com.dungeoncrawler.wearos.domain.usecase

import com.dungeoncrawler.wearos.core.haptics.HapticFeedbackManager
import com.dungeoncrawler.wearos.core.haptics.HapticPattern
import com.dungeoncrawler.wearos.domain.model.GameState
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
            in 3..4 -> resolveTrap(power.total.defense, power.total.damageReduction)
            else -> resolveMicroMob(progress.currentDungeonId, power.total.defense, power.total.damageReduction)
        } ?: return null

        gameProgressRepository.setGameState(GameState.MicroEventResolved(progress, event))
        hapticFeedbackManager.play(event.hapticPattern())
        return event
    }

    private suspend fun resolveLoot(dungeonId: String): MicroEvent? {
        val mob = monsterRepository.randomMicroMob(dungeonId) ?: return null
        val item = rollLoot(mob) ?: return null
        return MicroEvent.Loot(item)
    }

    private suspend fun resolveTrap(defense: Int, damageReduction: Int): MicroEvent {
        val raw = random.nextInt(6, 16)
        return MicroEvent.Trap(damage = applyDamage(raw, defense, damageReduction))
    }

    private suspend fun resolveMicroMob(
        dungeonId: String,
        defense: Int,
        damageReduction: Int,
    ): MicroEvent? {
        val mob = monsterRepository.randomMicroMob(dungeonId) ?: return null
        val raw = (mob.attack * random.nextDouble(0.4, 0.9)).roundToInt()
        return MicroEvent.MicroMobSlain(
            monster = mob,
            damageTaken = applyDamage(raw, defense, damageReduction),
            loot = rollLoot(mob),
        )
    }

    /** Shared mitigation path so defense and damage-reduction gear matter passively too. */
    private suspend fun applyDamage(raw: Int, defense: Int, damageReduction: Int): Int {
        val mitigated = ((raw - defense / 2) * (1f - damageReduction / 100f))
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
