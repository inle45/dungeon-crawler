package com.dungeoncrawler.wearos.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.dungeoncrawler.wearos.data.local.db.entity.EquipmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipment WHERE id = ${EquipmentEntity.SINGLETON_ID}")
    fun observe(): Flow<EquipmentEntity?>

    @Query("SELECT * FROM equipment WHERE id = ${EquipmentEntity.SINGLETON_ID}")
    suspend fun getOnce(): EquipmentEntity?

    @Upsert
    suspend fun upsert(entity: EquipmentEntity)
}
