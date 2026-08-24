package com.dungeoncrawler.wearos.presentation.combat

import androidx.lifecycle.viewModelScope
import com.dungeoncrawler.wearos.core.haptics.HapticFeedbackManager
import com.dungeoncrawler.wearos.core.haptics.HapticPattern
import com.dungeoncrawler.wearos.domain.model.CombatOutcome
import com.dungeoncrawler.wearos.domain.model.GameState
import com.dungeoncrawler.wearos.domain.repository.GameProgressRepository
import com.dungeoncrawler.wearos.domain.usecase.ExecuteCombatActionUseCase
import com.dungeoncrawler.wearos.presentation.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class BossCombatViewModel @Inject constructor(
    private val executeCombatActionUseCase: ExecuteCombatActionUseCase,
    private val gameProgressRepository: GameProgressRepository,
    private val hapticFeedbackManager: HapticFeedbackManager,
) : MviViewModel<BossCombatState, BossCombatIntent, BossCombatEffect>(BossCombatState()) {

    init {
        val current = gameProgressRepository.gameState.value
        if (current is GameState.BossEncounterTriggered) {
            setState { copy(player = current.player, boss = current.boss) }
            viewModelScope.launch {
                gameProgressRepository.setGameState(GameState.InCombat(current.player, current.boss))
            }
        } else if (current is GameState.InCombat) {
            setState { copy(player = current.player, boss = current.boss) }
        }

        viewModelScope.launch {
            gameProgressRepository.gameState.collectLatest { gameState ->
                when (gameState) {
                    is GameState.InCombat -> setState {
                        copy(
                            player = gameState.player,
                            boss = gameState.boss,
                            lastOutcome = gameState.lastOutcome,
                            isResolving = false,
                        )
                    }
                    is GameState.Victory, is GameState.Defeat -> sendEffect(BossCombatEffect.CombatEnded)
                    else -> Unit
                }
            }
        }
    }

    override suspend fun handleIntent(intent: BossCombatIntent) {
        when (intent) {
            is BossCombatIntent.SelectAction -> setState { copy(selectedIndex = intent.index) }
            is BossCombatIntent.RotateSelection -> setState {
                val next = (selectedIndex + intent.steps).mod(COMBAT_ACTIONS.size)
                copy(selectedIndex = next)
            }
            BossCombatIntent.ConfirmAction -> resolveAction()
        }
    }

    private suspend fun resolveAction() {
        val currentState = state.value
        if (currentState.isResolving) return
        setState { copy(isResolving = true) }

        val outcome = executeCombatActionUseCase(currentState.selectedAction, currentState.boss)
        hapticFeedbackManager.play(outcome.toHapticPattern())
    }

    private fun CombatOutcome.toHapticPattern(): HapticPattern = when (this) {
        is CombatOutcome.PlayerCriticalHit -> HapticPattern.CRITICAL_HIT
        is CombatOutcome.PlayerStandardHit -> HapticPattern.STANDARD_HIT
        is CombatOutcome.PlayerSpellCast -> HapticPattern.SPELL_CAST
        is CombatOutcome.PlayerParried -> HapticPattern.PARRY_SUCCESS
        is CombatOutcome.PlayerDamaged -> HapticPattern.DAMAGE_TAKEN
        is CombatOutcome.BossDefeated -> HapticPattern.CRITICAL_HIT
        CombatOutcome.PlayerDefeated -> HapticPattern.DAMAGE_TAKEN
    }
}
