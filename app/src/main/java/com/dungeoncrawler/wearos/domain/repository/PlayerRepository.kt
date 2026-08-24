package com.dungeoncrawler.wearos.domain.repository

import com.dungeoncrawler.wearos.domain.model.Equipment
import com.dungeoncrawler.wearos.domain.model.PlayerStats
import kotlinx.coroutines.flow.Flow

/** Player-facing view over the Room-backed persistence (stats, equipment, floor, step count). */
interface PlayerRepository {
    fun observePlayerStats(): Flow<PlayerStats>
    fun observeEquipment(): Flow<Equipment>
    suspend fun updatePlayerStats(transform: (PlayerStats) -> PlayerStats)
    suspend fun updateEquipment(transform: (Equipment) -> Equipment)
}
