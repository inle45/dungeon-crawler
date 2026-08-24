package com.dungeoncrawler.wearos.data.repository

import com.dungeoncrawler.wearos.data.local.db.dao.MonsterDao
import com.dungeoncrawler.wearos.data.local.db.entity.toDomain
import com.dungeoncrawler.wearos.domain.catalog.DungeonCatalog
import com.dungeoncrawler.wearos.domain.model.Dungeon
import com.dungeoncrawler.wearos.domain.model.Monster
import com.dungeoncrawler.wearos.domain.model.MonsterRole
import com.dungeoncrawler.wearos.domain.repository.MonsterRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class MonsterRepositoryImpl @Inject constructor(
    private val monsterDao: MonsterDao,
) : MonsterRepository {

    override suspend fun bestiaryOf(dungeonId: String): List<Monster> =
        monsterDao.ofDungeon(dungeonId).map { it.toDomain() }

    override suspend fun randomMicroMob(dungeonId: String): Monster? =
        monsterDao.ofDungeonAndRole(dungeonId, MonsterRole.MICRO_MOB.name)
            .randomOrNull(Random)
            ?.toDomain()

    override suspend fun bossForFloor(dungeonId: String, floorNumber: Int): Monster? {
        if (floorNumber >= Dungeon.FLOORS_PER_DUNGEON) {
            return monsterDao.ofDungeonAndRole(dungeonId, MonsterRole.SUPREME_BOSS.name)
                .firstOrNull()
                ?.toDomain()
        }

        val miniBosses = monsterDao.ofDungeonAndRole(dungeonId, MonsterRole.MINI_BOSS.name)
            .map { it.toDomain() }
            .sortedBy { it.maxHp }
        if (miniBosses.isEmpty()) return null

        // Ramp the four mini-bosses across floors 1-9 so each floor is a step up, not a re-roll.
        val index = ((floorNumber - 1) * miniBosses.size) / (Dungeon.FLOORS_PER_DUNGEON - 1)
        return miniBosses[index.coerceIn(0, miniBosses.lastIndex)]
    }
}
