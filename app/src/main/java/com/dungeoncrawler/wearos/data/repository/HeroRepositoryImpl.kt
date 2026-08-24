package com.dungeoncrawler.wearos.data.repository

import com.dungeoncrawler.wearos.data.local.db.dao.HeroDao
import com.dungeoncrawler.wearos.data.local.db.entity.HeroStateEntity
import com.dungeoncrawler.wearos.data.local.db.entity.toDungeonProgress
import com.dungeoncrawler.wearos.data.local.db.entity.toHeroStats
import com.dungeoncrawler.wearos.data.local.db.entity.withDungeonProgress
import com.dungeoncrawler.wearos.data.local.db.entity.withHeroStats
import com.dungeoncrawler.wearos.domain.model.DungeonProgress
import com.dungeoncrawler.wearos.domain.model.HeroStats
import com.dungeoncrawler.wearos.domain.repository.HeroRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class HeroRepositoryImpl @Inject constructor(
    private val heroDao: HeroDao,
) : HeroRepository {

    // Step deltas, combat turns and equip actions all mutate this single row from different
    // coroutines; the mutex keeps read-modify-write sequences from clobbering each other.
    private val writeLock = Mutex()

    override fun observeHeroStats(): Flow<HeroStats> =
        heroDao.observe().map { it?.toHeroStats() ?: HeroStats() }

    override fun observeDungeonProgress(): Flow<DungeonProgress> =
        heroDao.observe().map { (it ?: HeroStateEntity.default()).toDungeonProgress() }

    override suspend fun getHeroStats(): HeroStats = current().toHeroStats()

    override suspend fun getDungeonProgress(): DungeonProgress = current().toDungeonProgress()

    override suspend fun updateHeroStats(transform: (HeroStats) -> HeroStats) = writeLock.withLock {
        val entity = current()
        heroDao.upsert(entity.withHeroStats(transform(entity.toHeroStats())))
    }

    override suspend fun updateDungeonProgress(transform: (DungeonProgress) -> DungeonProgress) =
        writeLock.withLock {
            val entity = current()
            heroDao.upsert(entity.withDungeonProgress(transform(entity.toDungeonProgress())))
        }

    private suspend fun current(): HeroStateEntity =
        heroDao.getOnce() ?: HeroStateEntity.default().also { heroDao.upsert(it) }
}
