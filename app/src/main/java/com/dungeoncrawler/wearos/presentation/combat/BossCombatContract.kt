package com.dungeoncrawler.wearos.presentation.combat

import com.dungeoncrawler.wearos.domain.model.BossEncounter
import com.dungeoncrawler.wearos.domain.model.CombatAction
import com.dungeoncrawler.wearos.domain.model.CombatOutcome
import com.dungeoncrawler.wearos.domain.model.PlayerStats
import com.dungeoncrawler.wearos.presentation.mvi.MviEffect
import com.dungeoncrawler.wearos.presentation.mvi.MviIntent
import com.dungeoncrawler.wearos.presentation.mvi.MviState

val COMBAT_ACTIONS = listOf(CombatAction.ATTACK, CombatAction.DEFENSE, CombatAction.SPELL)

data class BossCombatState(
    val player: PlayerStats = PlayerStats(),
    val boss: BossEncounter = BossEncounter.forFloor(1),
    val selectedIndex: Int = 0,
    val lastOutcome: CombatOutcome? = null,
    val isResolving: Boolean = false,
) : MviState {
    val selectedAction: CombatAction get() = COMBAT_ACTIONS[selectedIndex]
}

sealed class BossCombatIntent : MviIntent {
    data class SelectAction(val index: Int) : BossCombatIntent()
    data class RotateSelection(val steps: Int) : BossCombatIntent()
    data object ConfirmAction : BossCombatIntent()
}

sealed class BossCombatEffect : MviEffect {
    data object CombatEnded : BossCombatEffect()
}
