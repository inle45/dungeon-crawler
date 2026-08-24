package com.dungeoncrawler.wearos.presentation.home

import androidx.lifecycle.viewModelScope
import com.dungeoncrawler.wearos.domain.model.GameState
import com.dungeoncrawler.wearos.domain.repository.GameProgressRepository
import com.dungeoncrawler.wearos.domain.repository.PlayerRepository
import com.dungeoncrawler.wearos.presentation.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val gameProgressRepository: GameProgressRepository,
) : MviViewModel<HomeState, HomeIntent, HomeEffect>(HomeState()) {

    init {
        viewModelScope.launch {
            combine(
                playerRepository.observePlayerStats(),
                playerRepository.observeEquipment(),
            ) { player, equipment -> player to equipment }
                .collectLatest { (player, equipment) ->
                    setState { copy(player = player, equipment = equipment) }
                }
        }

        viewModelScope.launch {
            gameProgressRepository.gameState.collectLatest { state ->
                when (state) {
                    is GameState.MicroEventResolved -> setState { copy(lastMicroEvent = state.event) }
                    is GameState.BossEncounterTriggered -> sendEffect(HomeEffect.NavigateToBossCombat(state.boss))
                    else -> Unit
                }
            }
        }
    }

    override suspend fun handleIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.DismissMicroEventBanner -> setState { copy(lastMicroEvent = null) }
        }
    }
}
