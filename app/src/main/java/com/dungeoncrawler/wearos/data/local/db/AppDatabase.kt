package com.dungeoncrawler.wearos.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dungeoncrawler.wearos.data.local.db.dao.EquipmentDao
import com.dungeoncrawler.wearos.data.local.db.dao.PlayerDao
import com.dungeoncrawler.wearos.data.local.db.entity.EquipmentEntity
import com.dungeoncrawler.wearos.data.local.db.entity.PlayerEntity

@Database(
    entities = [PlayerEntity::class, EquipmentEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun equipmentDao(): EquipmentDao

    companion object {
        const val DATABASE_NAME = "dungeon_crawler.db"
    }
}
