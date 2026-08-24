package com.dungeoncrawler.wearos.domain.repository

import com.dungeoncrawler.wearos.domain.model.Monster

/** Read-only access to the seeded bestiary. */
interface MonsterRepository {
    suspend fun bestiaryOf(dungeonId: String): List<Monster>
    suspend fun randomMicroMob(dungeonId: String): Monster?
    suspend fun bossForFloor(dungeonId: String, floorNumber: Int): Monster?
}
