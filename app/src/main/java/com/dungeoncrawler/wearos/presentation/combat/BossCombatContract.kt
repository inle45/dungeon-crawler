package com.dungeoncrawler.wearos.presentation.combat

import com.dungeoncrawler.wearos.domain.model.CombatAction
import com.dungeoncrawler.wearos.domain.model.CombatOutcome
import com.dungeoncrawler.wearos.domain.model.HeroPower
import com.dungeoncrawler.wearos.domain.model.Monster
import com.dungeoncrawler.wearos.presentation.mvi.MviEffect
import com.dungeoncrawler.wearos.presentation.mvi.MviIntent
import com.dungeoncrawler.wearos.presentation.mvi.MviState

val COMBAT_ACTIONS = listOf(CombatAction.ATTACK, CombatAction.DEFENSE, CombatAction.SPELL)

data class BossCombatState(
    val monster: Monster? = null,
    val monsterHp: Int = 0,
    val power: HeroPower? = null,
    val floorLabel: String = "",
    val selectedIndex: Int = 0,
    val lastOutcome: CombatOutcome? = null,
    val isResolving: Boolean = false,
) : MviState {
    val selectedAction: CombatAction get() = COMBAT_ACTIONS[selectedIndex]

    val monsterHpRatio: Float
        get() = monster?.let { if (it.maxHp == 0) 0f else monsterHp.toFloat() / it.maxHp } ?: 0f
}

sealed class BossCombatIntent : MviIntent {
    data class SelectAction(val index: Int) : BossCombatIntent()
    data class RotateSelection(val steps: Int) : BossCombatIntent()
    data object ConfirmAction : BossCombatIntent()
}

sealed class BossCombatEffect : MviEffect {
    /** A mini-boss died and the hero moved up a floor. */
    data object FloorCleared : BossCombatEffect()

    /** Floor 10's supreme boss died — hand over to `DungeonClearScreen`. */
    data object DungeonCleared : BossCombatEffect()

    data object Defeated : BossCombatEffect()
}
