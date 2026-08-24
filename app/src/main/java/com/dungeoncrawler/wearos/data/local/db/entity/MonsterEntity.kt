package com.dungeoncrawler.wearos.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dungeoncrawler.wearos.domain.model.LootDrop
import com.dungeoncrawler.wearos.domain.model.Monster
import com.dungeoncrawler.wearos.domain.model.MonsterRole

/**
 * One bestiary entry. The drop table is stored as a compact `itemId:weight` list — it is only
 * ever read whole, so a join table would cost more than it buys.
 */
@Entity(tableName = "monsters")
data class MonsterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val dungeonId: String,
    val role: String,
    val maxHp: Int,
    val attack: Int,
    val defense: Int,
    val dropTable: String,
    val spriteRes: String,
)

fun MonsterEntity.toDomain() = Monster(
    id = id,
    name = name,
    dungeonId = dungeonId,
    role = MonsterRole.valueOf(role),
    maxHp = maxHp,
    attack = attack,
    defense = defense,
    dropTable = dropTable.toDropTable(),
    spriteRes = spriteRes,
)

fun Monster.toEntity() = MonsterEntity(
    id = id,
    name = name,
    dungeonId = dungeonId,
    role = role.name,
    maxHp = maxHp,
    attack = attack,
    defense = defense,
    dropTable = dropTable.joinToString(",") { "${it.itemId}:${it.weight}" },
    spriteRes = spriteRes,
)

private fun String.toDropTable(): List<LootDrop> =
    split(",")
        .mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size != 2) return@mapNotNull null
            val weight = parts[1].toIntOrNull() ?: return@mapNotNull null
            LootDrop(itemId = parts[0], weight = weight)
        }
