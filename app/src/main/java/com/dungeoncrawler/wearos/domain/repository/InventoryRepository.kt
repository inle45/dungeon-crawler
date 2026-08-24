package com.dungeoncrawler.wearos.domain.repository

import com.dungeoncrawler.wearos.domain.model.EquipmentItem
import com.dungeoncrawler.wearos.domain.model.EquipmentSlot
import com.dungeoncrawler.wearos.domain.model.Loadout
import kotlinx.coroutines.flow.Flow

/** Everything the hero owns, plus which item occupies each slot. */
interface InventoryRepository {
    fun observeInventory(): Flow<List<EquipmentItem>>
    fun observeLoadout(): Flow<Loadout>

    suspend fun getInventory(): List<EquipmentItem>
    suspend fun getLoadout(): Loadout

    /** Adds a looted item to the bag. Returns false when the hero already owns it. */
    suspend fun addItem(item: EquipmentItem): Boolean

    /** Marks [itemId] equipped, unequipping whatever held its slot. */
    suspend fun equip(itemId: String)

    suspend fun unequip(slot: EquipmentSlot)

    /** Consumes a one-shot item, removing it from the bag. */
    suspend fun consume(itemId: String)
}
