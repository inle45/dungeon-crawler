package com.dungeoncrawler.wearos.data.local.db

import com.dungeoncrawler.wearos.data.local.db.dao.HeroDao
import com.dungeoncrawler.wearos.data.local.db.dao.InventoryDao
import com.dungeoncrawler.wearos.data.local.db.dao.MonsterDao
import com.dungeoncrawler.wearos.data.local.db.entity.HeroStateEntity
import com.dungeoncrawler.wearos.data.local.db.entity.toEntity
import com.dungeoncrawler.wearos.domain.catalog.DungeonCatalog
import com.dungeoncrawler.wearos.domain.catalog.EquipmentCatalog
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Writes the static catalogs into Room on first launch: the full bestiary, and the hero's
 * starter gear. Re-runs are cheap and idempotent — everything upserts by primary key.
 *
 * Note the inventory holds only what the hero *owns*: the rest of the catalog lives in
 * [com.dungeoncrawler.wearos.domain.catalog.GearIndex] and only reaches the table when it drops.
 */
@Singleton
class DatabaseSeeder @Inject constructor(
    private val heroDao: HeroDao,
    private val inventoryDao: InventoryDao,
    private val monsterDao: MonsterDao,
) {
    suspend fun seedIfNeeded() {
        if (heroDao.getOnce() == null) {
            heroDao.upsert(HeroStateEntity.default())
        }

        if (monsterDao.count() < DungeonCatalog.BESTIARY.size) {
            monsterDao.upsertAll(DungeonCatalog.BESTIARY.map { it.toEntity() })
        }

        if (inventoryDao.getAll().isEmpty()) {
            val starterIds = EquipmentCatalog.STARTER_LOADOUT_IDS.toSet()
            inventoryDao.upsertAll(
                EquipmentCatalog.ALL
                    .filter { it.id in starterIds }
                    .map { it.toEntity(isEquipped = true) },
            )
        }
    }
}
