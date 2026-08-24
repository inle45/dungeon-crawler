package com.dungeoncrawler.wearos.presentation.dungeon

import com.dungeoncrawler.wearos.domain.GameConstants
import com.dungeoncrawler.wearos.domain.catalog.DungeonCatalog
import com.dungeoncrawler.wearos.domain.model.Dungeon
import com.dungeoncrawler.wearos.domain.model.DungeonProgress
import com.dungeoncrawler.wearos.domain.model.HeroPower
import com.dungeoncrawler.wearos.domain.model.MicroEvent
import com.dungeoncrawler.wearos.presentation.mvi.MviEffect
import com.dungeoncrawler.wearos.presentation.mvi.MviIntent
import com.dungeoncrawler.wearos.presentation.mvi.MviState

data class DungeonNavigationState(
    val dungeon: Dungeon = DungeonCatalog.FIRST,
    val progress: DungeonProgress = DungeonProgress(DungeonCatalog.FIRST.id),
    val power: HeroPower? = null,
    val totalSteps: Long = 0,
    val lastMicroEvent: MicroEvent? = null,
) : MviState {

    val floorLabel: String get() = progress.floorLabel

    val stepsIntoBossCycle: Long get() = totalSteps % GameConstants.STEPS_PER_BOSS_ENCOUNTER

    val stepsToNextBoss: Long get() = GameConstants.STEPS_PER_BOSS_ENCOUNTER - stepsIntoBossCycle

    val bossProgressRatio: Float
        get() = stepsIntoBossCycle.toFloat() / GameConstants.STEPS_PER_BOSS_ENCOUNTER

    val floorProgressRatio: Float
        get() = progress.currentFloor.toFloat() / Dungeon.FLOORS_PER_DUNGEON

    val isFinalFloor: Boolean get() = progress.isFinalFloor
}

sealed class DungeonNavigationIntent : MviIntent {
    data object DismissMicroEvent : DungeonNavigationIntent()
    data object OpenInventory : DungeonNavigationIntent()
}

sealed class DungeonNavigationEffect : MviEffect {
    data object NavigateToBossCombat : DungeonNavigationEffect()
    data object NavigateToInventory : DungeonNavigationEffect()
    data object NavigateToDungeonClear : DungeonNavigationEffect()
}
