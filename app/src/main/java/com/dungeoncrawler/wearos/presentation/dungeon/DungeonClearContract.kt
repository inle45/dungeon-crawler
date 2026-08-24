package com.dungeoncrawler.wearos.presentation.dungeon

import com.dungeoncrawler.wearos.domain.catalog.DungeonCatalog
import com.dungeoncrawler.wearos.domain.model.Dungeon
import com.dungeoncrawler.wearos.domain.model.DungeonClearChoice
import com.dungeoncrawler.wearos.domain.model.EquipmentItem
import com.dungeoncrawler.wearos.domain.model.Monster
import com.dungeoncrawler.wearos.presentation.mvi.MviEffect
import com.dungeoncrawler.wearos.presentation.mvi.MviIntent
import com.dungeoncrawler.wearos.presentation.mvi.MviState

data class DungeonClearState(
    val dungeon: Dungeon = DungeonCatalog.FIRST,
    val supremeBoss: Monster? = null,
    val loot: EquipmentItem? = null,
    val nextDungeon: Dungeon? = null,
    val selectedIndex: Int = 0,
    val isApplying: Boolean = false,
) : MviState {

    /** With no dungeon left to unlock, farming the current one is the only offer. */
    val choices: List<DungeonClearChoice>
        get() = if (nextDungeon == null) {
            listOf(DungeonClearChoice.FARM_AGAIN)
        } else {
            listOf(DungeonClearChoice.FARM_AGAIN, DungeonClearChoice.NEXT_DUNGEON)
        }

    val selectedChoice: DungeonClearChoice
        get() = choices[selectedIndex.coerceIn(choices.indices)]
}

sealed class DungeonClearIntent : MviIntent {
    data class SelectChoice(val index: Int) : DungeonClearIntent()
    data class RotateSelection(val steps: Int) : DungeonClearIntent()
    data class Confirm(val choice: DungeonClearChoice) : DungeonClearIntent()
}

sealed class DungeonClearEffect : MviEffect {
    data class ChoiceApplied(val choice: DungeonClearChoice) : DungeonClearEffect()
}
