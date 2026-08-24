package com.dungeoncrawler.wearos.presentation.inventory

import com.dungeoncrawler.wearos.domain.model.EquipmentItem
import com.dungeoncrawler.wearos.domain.model.HeroPower
import com.dungeoncrawler.wearos.domain.model.StatBlock
import com.dungeoncrawler.wearos.presentation.mvi.MviEffect
import com.dungeoncrawler.wearos.presentation.mvi.MviIntent
import com.dungeoncrawler.wearos.presentation.mvi.MviState

data class InventoryState(
    val items: List<EquipmentItem> = emptyList(),
    val power: HeroPower? = null,
    /** Item the player is currently inspecting; drives the stat comparator. */
    val selectedItem: EquipmentItem? = null,
) : MviState {

    val equippedIds: Set<String> = power?.loadout?.equippedItems?.map { it.id }?.toSet().orEmpty()

    fun isEquipped(item: EquipmentItem): Boolean = equippedIds.contains(item.id)

    /**
     * Stat delta the player would see by equipping [selectedItem]: the candidate's contribution
     * minus whatever currently holds its slot. Green when it goes up, red when it goes down.
     *
     * Compares effective stats, so an epic's multiplier is part of what the player is shown
     * rather than a hidden bonus they only notice after equipping.
     */
    val comparison: StatBlock?
        get() {
            val candidate = selectedItem ?: return null
            if (candidate.isConsumable) return null
            val current = power?.loadout?.get(candidate.slot)
            return candidate.effectiveStats - (current?.effectiveStats ?: StatBlock.EMPTY)
        }

    /** The item currently occupying the inspected item's slot, if any. */
    val replacedItem: EquipmentItem?
        get() = selectedItem?.let { power?.loadout?.get(it.slot) }
}

sealed class InventoryIntent : MviIntent {
    data class SelectItem(val item: EquipmentItem?) : InventoryIntent()
    data class EquipSelected(val item: EquipmentItem) : InventoryIntent()
}

sealed class InventoryEffect : MviEffect {
    data class Equipped(val item: EquipmentItem) : InventoryEffect()
    data class Consumed(val item: EquipmentItem, val healedTo: Int) : InventoryEffect()
}
