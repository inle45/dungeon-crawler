package com.dungeoncrawler.wearos.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dungeoncrawler.wearos.data.local.db.dao.HeroDao
import com.dungeoncrawler.wearos.data.local.db.dao.InventoryDao
import com.dungeoncrawler.wearos.data.local.db.dao.MonsterDao
import com.dungeoncrawler.wearos.data.local.db.entity.HeroStateEntity
import com.dungeoncrawler.wearos.data.local.db.entity.InventoryItemEntity
import com.dungeoncrawler.wearos.data.local.db.entity.MonsterEntity

@Database(
    entities = [HeroStateEntity::class, InventoryItemEntity::class, MonsterEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun heroDao(): HeroDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun monsterDao(): MonsterDao

    companion object {
        const val DATABASE_NAME = "dungeon_crawler.db"
    }
}
