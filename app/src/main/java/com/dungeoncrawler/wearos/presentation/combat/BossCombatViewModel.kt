package com.dungeoncrawler.wearos.presentation.combat

import androidx.lifecycle.viewModelScope
import com.dungeoncrawler.wearos.core.haptics.HapticFeedbackManager
import com.dungeoncrawler.wearos.core.haptics.HapticPattern
import com.dungeoncrawler.wearos.domain.model.CombatOutcome
import com.dungeoncrawler.wearos.domain.model.GameState
import com.dungeoncrawler.wearos.domain.repository.GameProgressRepository
import com.dungeoncrawler.wearos.domain.usecase.ComputeHeroPowerUseCase
import com.dungeoncrawler.wearos.domain.usecase.ExecuteCombatActionUseCase
import com.dungeoncrawler.wearos.presentation.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class BossCombatViewModel @Inject constructor(
    private val executeCombatAction: ExecuteCombatActionUseCase,
    private val computeHeroPower: ComputeHeroPowerUseCase,
    private val gameProgressRepository: GameProgressRepository,
    private val hapticFeedbackManager: HapticFeedbackManager,
) : MviViewModel<BossCombatState, BossCombatIntent, BossCombatEffect>(BossCombatState()) {

    init {
        viewModelScope.launch {
            // Promote the pending encounter into an active fight the first time we land here.
            val pending = gameProgressRepository.gameState.value
            if (pending is GameState.BossEncounterTriggered) {
                gameProgressRepository.setGameState(
                    GameState.InCombat(
                        progress = pending.progress,
                        monster = pending.monster,
                        monsterHp = pending.monster.maxHp,
                        heroHp = computeHeroPower.once().currentHp,
                    ),
                )
            }
        }

        viewModelScope.launch {
            computeHeroPower().collectLatest { power -> setState { copy(power = power) } }
        }

        viewModelScope.launch {
            gameProgressRepository.gameState.collectLatest { gameState ->
                when (gameState) {
                    is GameState.InCombat -> setState {
                        copy(
                            monster = gameState.monster,
                            monsterHp = gameState.monsterHp,
                            floorLabel = gameState.progress.floorLabel,
                            lastOutcome = gameState.lastOutcome,
                            isResolving = false,
                        )
                    }
                    is GameState.FloorCleared -> sendEffect(BossCombatEffect.FloorCleared)
                    is GameState.DungeonCleared -> sendEffect(BossCombatEffect.DungeonCleared)
                    is GameState.Defeat -> sendEffect(BossCombatEffect.Defeated)
                    else -> Unit
                }
            }
        }
    }

    override suspend fun handleIntent(intent: BossCombatIntent) {
        when (intent) {
            is BossCombatIntent.SelectAction -> setState { copy(selectedIndex = intent.index) }

            is BossCombatIntent.RotateSelection -> setState {
                copy(selectedIndex = (selectedIndex + intent.steps).mod(COMBAT_ACTIONS.size))
            }

            BossCombatIntent.ConfirmAction -> resolveAction()
        }
    }

    private suspend fun resolveAction() {
        val current = state.value
        val monster = current.monster ?: return
        if (current.isResolving) return

        setState { copy(isResolving = true) }
        val outcome = executeCombatAction(current.selectedAction, monster, current.monsterHp)
        hapticFeedbackManager.play(outcome.toHapticPattern())
    }

    private fun CombatOutcome.toHapticPattern(): HapticPattern = when (this) {
        is CombatOutcome.PlayerCriticalHit -> HapticPattern.CRITICAL_HIT
        is CombatOutcome.PlayerStandardHit -> HapticPattern.STANDARD_HIT
        is CombatOutcome.PlayerSpellCast -> HapticPattern.SPELL_CAST
        is CombatOutcome.PlayerParried -> HapticPattern.PARRY_SUCCESS
        is CombatOutcome.PlayerDamaged -> HapticPattern.DAMAGE_TAKEN
        is CombatOutcome.MonsterSlain -> HapticPattern.CRITICAL_HIT
        is CombatOutcome.PlayerDefeated -> HapticPattern.DAMAGE_TAKEN
    }
}
