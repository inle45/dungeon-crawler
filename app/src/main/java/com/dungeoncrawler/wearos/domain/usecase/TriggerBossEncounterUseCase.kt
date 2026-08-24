package com.dungeoncrawler.wearos.domain.usecase

import com.dungeoncrawler.wearos.core.haptics.HapticFeedbackManager
import com.dungeoncrawler.wearos.core.haptics.HapticPattern
import com.dungeoncrawler.wearos.domain.model.BossEncounter
import com.dungeoncrawler.wearos.domain.model.GameState
import com.dungeoncrawler.wearos.domain.repository.GameProgressRepository
import com.dungeoncrawler.wearos.domain.repository.PlayerRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Fires once every 2000 steps: spawns the floor's boss and alerts the player with haptics. */
class TriggerBossEncounterUseCase @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val gameProgressRepository: GameProgressRepository,
    private val hapticFeedbackManager: HapticFeedbackManager,
) {
    suspend operator fun invoke() {
        val player = playerRepository.observePlayerStats().first()
        val boss = BossEncounter.forFloor(player.currentFloor)
        gameProgressRepository.setGameState(GameState.BossEncounterTriggered(player, boss))
        hapticFeedbackManager.play(HapticPattern.BOSS_ALERT)
    }
}
