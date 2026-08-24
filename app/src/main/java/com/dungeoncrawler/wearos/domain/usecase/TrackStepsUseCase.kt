package com.dungeoncrawler.wearos.domain.usecase

import com.dungeoncrawler.wearos.domain.GameConstants
import com.dungeoncrawler.wearos.domain.repository.HealthRepository
import com.dungeoncrawler.wearos.domain.repository.HeroRepository
import javax.inject.Inject

/**
 * Consumes step deltas from Health Services, persists the running total, and fires the
 * micro-event / boss-encounter thresholds as they're crossed.
 */
class TrackStepsUseCase @Inject constructor(
    private val healthRepository: HealthRepository,
    private val heroRepository: HeroRepository,
    private val resolveMicroEventUseCase: ResolveMicroEventUseCase,
    private val triggerBossEncounterUseCase: TriggerBossEncounterUseCase,
) {
    suspend operator fun invoke() {
        healthRepository.observeStepDelta().collect { stepDelta ->
            if (stepDelta <= 0) return@collect

            var previousTotal = 0L
            var newTotal = 0L
            heroRepository.updateHeroStats { hero ->
                previousTotal = hero.totalSteps
                newTotal = hero.totalSteps + stepDelta
                hero.copy(totalSteps = newTotal)
            }

            val microEventsCrossed = newTotal / GameConstants.STEPS_PER_MICRO_EVENT -
                previousTotal / GameConstants.STEPS_PER_MICRO_EVENT
            repeat(microEventsCrossed.toInt()) { resolveMicroEventUseCase() }

            val bossThresholdsCrossed = newTotal / GameConstants.STEPS_PER_BOSS_ENCOUNTER -
                previousTotal / GameConstants.STEPS_PER_BOSS_ENCOUNTER
            if (bossThresholdsCrossed > 0) {
                triggerBossEncounterUseCase()
            }
        }
    }
}
