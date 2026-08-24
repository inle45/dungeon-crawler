package com.dungeoncrawler.wearos.domain.repository

import com.dungeoncrawler.wearos.domain.model.DungeonProgress
import com.dungeoncrawler.wearos.domain.model.HeroStats
import kotlinx.coroutines.flow.Flow

/** Hero base stats, current HP, walked steps and dungeon/floor position — all Room-backed. */
interface HeroRepository {
    fun observeHeroStats(): Flow<HeroStats>
    fun observeDungeonProgress(): Flow<DungeonProgress>

    suspend fun getHeroStats(): HeroStats
    suspend fun getDungeonProgress(): DungeonProgress

    suspend fun updateHeroStats(transform: (HeroStats) -> HeroStats)
    suspend fun updateDungeonProgress(transform: (DungeonProgress) -> DungeonProgress)
}
