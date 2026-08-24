package com.dungeoncrawler.wearos.data.repository

import com.dungeoncrawler.wearos.data.local.db.dao.EquipmentDao
import com.dungeoncrawler.wearos.data.local.db.dao.PlayerDao
import com.dungeoncrawler.wearos.data.local.db.entity.EquipmentEntity
import com.dungeoncrawler.wearos.data.local.db.entity.PlayerEntity
import com.dungeoncrawler.wearos.data.local.db.entity.toDomain
import com.dungeoncrawler.wearos.data.local.db.entity.toEntity
import com.dungeoncrawler.wearos.domain.model.Equipment
import com.dungeoncrawler.wearos.domain.model.PlayerStats
import com.dungeoncrawler.wearos.domain.repository.PlayerRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class PlayerRepositoryImpl @Inject constructor(
    private val playerDao: PlayerDao,
    private val equipmentDao: EquipmentDao,
) : PlayerRepository {

    override fun observePlayerStats(): Flow<PlayerStats> =
        playerDao.observe().map { it?.toDomain() ?: PlayerStats() }

    override fun observeEquipment(): Flow<Equipment> =
        equipmentDao.observe().map { it?.toDomain() ?: Equipment() }

    override suspend fun updatePlayerStats(transform: (PlayerStats) -> PlayerStats) {
        val current = playerDao.getOnce()?.toDomain() ?: PlayerStats()
        val updated = transform(current)
        playerDao.upsert(updated.toEntity().copy(id = PlayerEntity.SINGLETON_ID))
    }

    override suspend fun updateEquipment(transform: (Equipment) -> Equipment) {
        val current = equipmentDao.getOnce()?.toDomain() ?: Equipment()
        val updated = transform(current)
        equipmentDao.upsert(updated.toEntity().copy(id = EquipmentEntity.SINGLETON_ID))
    }
}
