package com.dungeoncrawler.wearos.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dungeoncrawler.wearos.domain.model.PlayerStats

@Entity(tableName = "player_stats")
data class PlayerEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val level: Int,
    val maxHp: Int,
    val currentHp: Int,
    val baseAttack: Int,
    val baseDefense: Int,
    val baseMagicPower: Int,
    val xp: Int,
    val totalSteps: Long,
    val currentFloor: Int,
) {
    companion object {
        const val SINGLETON_ID = 0

        fun default() = PlayerStats().toEntity()
    }
}

fun PlayerEntity.toDomain() = PlayerStats(
    level = level,
    maxHp = maxHp,
    currentHp = currentHp,
    baseAttack = baseAttack,
    baseDefense = baseDefense,
    baseMagicPower = baseMagicPower,
    xp = xp,
    totalSteps = totalSteps,
    currentFloor = currentFloor,
)

fun PlayerStats.toEntity() = PlayerEntity(
    level = level,
    maxHp = maxHp,
    currentHp = currentHp,
    baseAttack = baseAttack,
    baseDefense = baseDefense,
    baseMagicPower = baseMagicPower,
    xp = xp,
    totalSteps = totalSteps,
    currentFloor = currentFloor,
)
