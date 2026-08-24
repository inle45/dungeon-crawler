package com.dungeoncrawler.wearos.domain.usecase

import com.dungeoncrawler.wearos.core.haptics.HapticFeedbackManager
import com.dungeoncrawler.wearos.core.haptics.HapticPattern
import com.dungeoncrawler.wearos.domain.model.GameState
import com.dungeoncrawler.wearos.domain.model.Monster
import com.dungeoncrawler.wearos.domain.repository.GameProgressRepository
import com.dungeoncrawler.wearos.domain.repository.HeroRepository
import com.dungeoncrawler.wearos.domain.repository.MonsterRepository
import javax.inject.Inject

/**
 * Fires once every 2000 steps: spawns the floor's boss — a mini-boss on floors 1-9, the dungeon's
 * supreme boss on floor 10 — and alerts the player with the dedicated haptic pattern.
 */
class TriggerBossEncounterUseCase @Inject constructor(
    private val heroRepository: HeroRepository,
    private val monsterRepository: MonsterRepository,
    private val gameProgressRepository: GameProgressRepository,
    private val hapticFeedbackManager: HapticFeedbackManager,
) {
    suspend operator fun invoke(): Monster? {
        val progress = heroRepository.getDungeonProgress()
        val boss = monsterRepository.bossForFloor(progress.currentDungeonId, progress.currentFloor)
            ?: return null

        gameProgressRepository.setGameState(GameState.BossEncounterTriggered(progress, boss))
        hapticFeedbackManager.play(HapticPattern.BOSS_ALERT)
        return boss
    }
}
