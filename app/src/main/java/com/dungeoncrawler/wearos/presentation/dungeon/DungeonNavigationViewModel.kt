package com.dungeoncrawler.wearos.presentation.dungeon

import androidx.lifecycle.viewModelScope
import com.dungeoncrawler.wearos.domain.catalog.DungeonCatalog
import com.dungeoncrawler.wearos.domain.model.GameState
import com.dungeoncrawler.wearos.domain.repository.GameProgressRepository
import com.dungeoncrawler.wearos.domain.repository.HeroRepository
import com.dungeoncrawler.wearos.domain.usecase.ComputeHeroPowerUseCase
import com.dungeoncrawler.wearos.presentation.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class DungeonNavigationViewModel @Inject constructor(
    heroRepository: HeroRepository,
    computeHeroPower: ComputeHeroPowerUseCase,
    private val gameProgressRepository: GameProgressRepository,
) : MviViewModel<DungeonNavigationState, DungeonNavigationIntent, DungeonNavigationEffect>(
    DungeonNavigationState(),
) {

    init {
        viewModelScope.launch {
            combine(
                heroRepository.observeHeroStats(),
                heroRepository.observeDungeonProgress(),
                computeHeroPower(),
            ) { hero, progress, power -> Triple(hero, progress, power) }
                .collectLatest { (hero, progress, power) ->
                    setState {
                        copy(
                            dungeon = DungeonCatalog.findById(progress.currentDungeonId)
                                ?: DungeonCatalog.FIRST,
                            progress = progress,
                            power = power,
                            totalSteps = hero.totalSteps,
                        )
                    }
                }
        }

        viewModelScope.launch {
            gameProgressRepository.gameState.collectLatest { gameState ->
                when (gameState) {
                    is GameState.MicroEventResolved -> setState { copy(lastMicroEvent = gameState.event) }
                    is GameState.BossEncounterTriggered ->
                        sendEffect(DungeonNavigationEffect.NavigateToBossCombat)
                    is GameState.DungeonCleared ->
                        sendEffect(DungeonNavigationEffect.NavigateToDungeonClear)
                    else -> Unit
                }
            }
        }
    }

    override suspend fun handleIntent(intent: DungeonNavigationIntent) {
        when (intent) {
            DungeonNavigationIntent.DismissMicroEvent -> setState { copy(lastMicroEvent = null) }
            DungeonNavigationIntent.OpenInventory ->
                sendEffect(DungeonNavigationEffect.NavigateToInventory)
        }
    }
}
