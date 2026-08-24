package com.dungeoncrawler.wearos.presentation.dungeon

import com.dungeoncrawler.wearos.core.haptics.HapticFeedbackManager
import com.dungeoncrawler.wearos.core.haptics.HapticPattern
import com.dungeoncrawler.wearos.domain.model.DungeonClearChoice
import com.dungeoncrawler.wearos.domain.model.GameState
import com.dungeoncrawler.wearos.domain.repository.GameProgressRepository
import com.dungeoncrawler.wearos.domain.usecase.AdvanceFloorUseCase
import com.dungeoncrawler.wearos.presentation.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DungeonClearViewModel @Inject constructor(
    private val gameProgressRepository: GameProgressRepository,
    private val advanceFloor: AdvanceFloorUseCase,
    private val hapticFeedbackManager: HapticFeedbackManager,
) : MviViewModel<DungeonClearState, DungeonClearIntent, DungeonClearEffect>(DungeonClearState()) {

    init {
        val cleared = gameProgressRepository.gameState.value as? GameState.DungeonCleared
        if (cleared != null) {
            setState {
                copy(
                    dungeon = cleared.dungeon,
                    supremeBoss = cleared.supremeBoss,
                    loot = cleared.loot,
                    nextDungeon = cleared.nextDungeon,
                    // A cleared dungeon is worth its own fanfare, not a reused combat buzz.
                    selectedIndex = if (cleared.nextDungeon != null) 1 else 0,
                )
            }
            hapticFeedbackManager.play(HapticPattern.BOSS_ALERT)
        }
    }

    override suspend fun handleIntent(intent: DungeonClearIntent) {
        when (intent) {
            is DungeonClearIntent.SelectChoice -> setState {
                copy(selectedIndex = intent.index.coerceIn(choices.indices))
            }

            is DungeonClearIntent.RotateSelection -> setState {
                copy(selectedIndex = (selectedIndex + intent.steps).mod(choices.size))
            }

            is DungeonClearIntent.Confirm -> applyChoice(intent.choice)
        }
    }

    private suspend fun applyChoice(choice: DungeonClearChoice) {
        if (state.value.isApplying) return
        setState { copy(isApplying = true) }

        val progress = advanceFloor.applyClearChoice(choice)
        gameProgressRepository.setGameState(GameState.Exploring(progress))

        hapticFeedbackManager.play(
            when (choice) {
                DungeonClearChoice.FARM_AGAIN -> HapticPattern.PARRY_SUCCESS
                DungeonClearChoice.NEXT_DUNGEON -> HapticPattern.CRITICAL_HIT
            },
        )
        sendEffect(DungeonClearEffect.ChoiceApplied(choice))
    }
}
