package com.dungeoncrawler.wearos.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.dungeoncrawler.wearos.data.local.db.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player_stats WHERE id = ${PlayerEntity.SINGLETON_ID}")
    fun observe(): Flow<PlayerEntity?>

    @Query("SELECT * FROM player_stats WHERE id = ${PlayerEntity.SINGLETON_ID}")
    suspend fun getOnce(): PlayerEntity?

    @Upsert
    suspend fun upsert(entity: PlayerEntity)
}
