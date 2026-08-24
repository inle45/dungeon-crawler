package com.dungeoncrawler.wearos.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.dungeoncrawler.wearos.data.local.db.entity.MonsterEntity

@Dao
interface MonsterDao {
    @Query("SELECT * FROM monsters WHERE dungeonId = :dungeonId")
    suspend fun ofDungeon(dungeonId: String): List<MonsterEntity>

    @Query("SELECT * FROM monsters WHERE dungeonId = :dungeonId AND role = :role")
    suspend fun ofDungeonAndRole(dungeonId: String, role: String): List<MonsterEntity>

    @Query("SELECT COUNT(*) FROM monsters")
    suspend fun count(): Int

    @Upsert
    suspend fun upsertAll(entities: List<MonsterEntity>)
}
