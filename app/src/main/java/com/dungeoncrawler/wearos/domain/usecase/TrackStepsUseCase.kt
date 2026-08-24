package com.dungeoncrawler.wearos.domain.usecase

import com.dungeoncrawler.wearos.domain.GameConstants
import com.dungeoncrawler.wearos.domain.repository.HealthRepository
import com.dungeoncrawler.wearos.domain.repository.PlayerRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.collect

/**
 * Consumes step deltas from Health Services, persists the running total, and fires the
 * micro-event / boss-encounter thresholds as they're crossed.
 */
class TrackStepsUseCase @Inject constructor(
    private val healthRepository: HealthRepository,
    private val playerRepository: PlayerRepository,
    private val resolveMicroEventUseCase: ResolveMicroEventUseCase,
    private val triggerBossEncounterUseCase: TriggerBossEncounterUseCase,
) {
    suspend operator fun invoke() {
        healthRepository.observeStepDelta().collect { stepDelta ->
            if (stepDelta <= 0) return@collect

            var previousTotal = 0L
            var newTotal = 0L
            playerRepository.updatePlayerStats { stats ->
                previousTotal = stats.totalSteps
                newTotal = stats.totalSteps + stepDelta
                stats.copy(totalSteps = newTotal)
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
