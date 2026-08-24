package com.dungeoncrawler.wearos.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dungeoncrawler.wearos.domain.catalog.DungeonCatalog
import com.dungeoncrawler.wearos.domain.model.DungeonProgress
import com.dungeoncrawler.wearos.domain.model.HeroStats
import com.dungeoncrawler.wearos.domain.model.StatBlock

/** Single-row table holding the hero's base stat line, HP, step count and dungeon position. */
@Entity(tableName = "hero_state")
data class HeroStateEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val currentHp: Int,
    val baseMaxHp: Int,
    val baseAttack: Int,
    val baseDefense: Int,
    val baseCritRate: Int,
    val baseMagicPower: Int,
    val totalSteps: Long,
    val currentDungeonId: String,
    val currentFloor: Int,
    /** Comma-separated dungeon ids; the set is tiny and never queried by element. */
    val unlockedDungeonIds: String,
    val clearedDungeonIds: String,
) {
    companion object {
        const val SINGLETON_ID = 0

        fun default(): HeroStateEntity {
            val base = HeroStats.BASE
            return HeroStateEntity(
                currentHp = base.maxHp,
                baseMaxHp = base.maxHp,
                baseAttack = base.attack,
                baseDefense = base.defense,
                baseCritRate = base.critRate,
                baseMagicPower = base.magicPower,
                totalSteps = 0,
                currentDungeonId = DungeonCatalog.FIRST.id,
                currentFloor = 1,
                unlockedDungeonIds = DungeonCatalog.FIRST.id,
                clearedDungeonIds = "",
            )
        }
    }
}

fun HeroStateEntity.toHeroStats() = HeroStats(
    currentHp = currentHp,
    base = StatBlock(
        maxHp = baseMaxHp,
        attack = baseAttack,
        defense = baseDefense,
        critRate = baseCritRate,
        magicPower = baseMagicPower,
    ),
    totalSteps = totalSteps,
)

fun HeroStateEntity.toDungeonProgress() = DungeonProgress(
    currentDungeonId = currentDungeonId,
    currentFloor = currentFloor,
    unlockedDungeonIds = unlockedDungeonIds.toIdSet().ifEmpty { setOf(currentDungeonId) },
    clearedDungeonIds = clearedDungeonIds.toIdSet(),
)

fun HeroStateEntity.withHeroStats(stats: HeroStats) = copy(
    currentHp = stats.currentHp,
    baseMaxHp = stats.base.maxHp,
    baseAttack = stats.base.attack,
    baseDefense = stats.base.defense,
    baseCritRate = stats.base.critRate,
    baseMagicPower = stats.base.magicPower,
    totalSteps = stats.totalSteps,
)

fun HeroStateEntity.withDungeonProgress(progress: DungeonProgress) = copy(
    currentDungeonId = progress.currentDungeonId,
    currentFloor = progress.currentFloor,
    unlockedDungeonIds = progress.unlockedDungeonIds.joinToString(","),
    clearedDungeonIds = progress.clearedDungeonIds.joinToString(","),
)

private fun String.toIdSet(): Set<String> =
    split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
