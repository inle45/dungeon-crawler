package com.dungeoncrawler.wearos.data.repository

import com.dungeoncrawler.wearos.data.local.db.dao.InventoryDao
import com.dungeoncrawler.wearos.data.local.db.entity.InventoryItemEntity
import com.dungeoncrawler.wearos.data.local.db.entity.toDomain
import com.dungeoncrawler.wearos.data.local.db.entity.toEntity
import com.dungeoncrawler.wearos.domain.model.EquipmentItem
import com.dungeoncrawler.wearos.domain.model.EquipmentSlot
import com.dungeoncrawler.wearos.domain.model.Loadout
import com.dungeoncrawler.wearos.domain.repository.InventoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class InventoryRepositoryImpl @Inject constructor(
    private val inventoryDao: InventoryDao,
) : InventoryRepository {

    override fun observeInventory(): Flow<List<EquipmentItem>> =
        inventoryDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeLoadout(): Flow<Loadout> =
        inventoryDao.observeEquipped().map { entities -> entities.toLoadout() }

    override suspend fun getInventory(): List<EquipmentItem> =
        inventoryDao.getAll().map { it.toDomain() }

    override suspend fun getLoadout(): Loadout = inventoryDao.getEquipped().toLoadout()

    override suspend fun addItem(item: EquipmentItem): Boolean {
        if (inventoryDao.findById(item.id) != null) return false
        inventoryDao.upsert(item.toEntity())
        return true
    }

    override suspend fun equip(itemId: String) {
        val entity = inventoryDao.findById(itemId) ?: return
        inventoryDao.equipExclusively(itemId = itemId, slot = entity.slot)
    }

    override suspend fun unequip(slot: EquipmentSlot) {
        inventoryDao.clearSlot(slot.name)
    }

    override suspend fun consume(itemId: String) {
        inventoryDao.delete(itemId)
    }

    private fun List<InventoryItemEntity>.toLoadout() =
        Loadout(
            map { it.toDomain() }
                // A consumable sitting in the bag is never an equipped stat source; it is used, not worn.
                .filterNot { it.isConsumable }
                .associateBy { it.slot },
        )
}
