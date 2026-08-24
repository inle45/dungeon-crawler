package com.dungeoncrawler.wearos.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.dungeoncrawler.wearos.data.local.db.entity.InventoryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items")
    fun observeAll(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items")
    suspend fun getAll(): List<InventoryItemEntity>

    @Query("SELECT * FROM inventory_items WHERE isEquipped = 1")
    fun observeEquipped(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE isEquipped = 1")
    suspend fun getEquipped(): List<InventoryItemEntity>

    @Query("SELECT * FROM inventory_items WHERE id = :itemId")
    suspend fun findById(itemId: String): InventoryItemEntity?

    @Upsert
    suspend fun upsert(entity: InventoryItemEntity)

    @Upsert
    suspend fun upsertAll(entities: List<InventoryItemEntity>)

    @Query("DELETE FROM inventory_items WHERE id = :itemId")
    suspend fun delete(itemId: String)

    @Query("UPDATE inventory_items SET isEquipped = 0 WHERE slot = :slot")
    suspend fun clearSlot(slot: String)

    @Query("UPDATE inventory_items SET isEquipped = 1 WHERE id = :itemId")
    suspend fun markEquipped(itemId: String)

    /** Swaps the slot's occupant atomically so no frame ever sees two items in one slot. */
    @Transaction
    suspend fun equipExclusively(itemId: String, slot: String) {
        clearSlot(slot)
        markEquipped(itemId)
    }
}
