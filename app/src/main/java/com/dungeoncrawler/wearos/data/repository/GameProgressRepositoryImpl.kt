package com.dungeoncrawler.wearos.data.repository

import com.dungeoncrawler.wearos.domain.model.GameState
import com.dungeoncrawler.wearos.domain.repository.GameProgressRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class GameProgressRepositoryImpl @Inject constructor() : GameProgressRepository {

    private val _gameState = MutableStateFlow<GameState>(GameState.Idle)
    override val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    override suspend fun setGameState(state: GameState) {
        _gameState.value = state
    }
}
