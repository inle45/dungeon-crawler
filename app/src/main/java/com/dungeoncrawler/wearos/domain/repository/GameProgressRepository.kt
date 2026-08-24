package com.dungeoncrawler.wearos.domain.repository

import com.dungeoncrawler.wearos.domain.model.GameState
import kotlinx.coroutines.flow.StateFlow

interface GameProgressRepository {
    val gameState: StateFlow<GameState>
    suspend fun setGameState(state: GameState)
}
