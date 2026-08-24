package com.dungeoncrawler.wearos.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.dungeoncrawler.wearos.data.local.db.entity.HeroStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeroDao {
    @Query("SELECT * FROM hero_state WHERE id = ${HeroStateEntity.SINGLETON_ID}")
    fun observe(): Flow<HeroStateEntity?>

    @Query("SELECT * FROM hero_state WHERE id = ${HeroStateEntity.SINGLETON_ID}")
    suspend fun getOnce(): HeroStateEntity?

    @Upsert
    suspend fun upsert(entity: HeroStateEntity)
}
