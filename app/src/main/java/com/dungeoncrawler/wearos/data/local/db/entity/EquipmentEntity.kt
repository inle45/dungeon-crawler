package com.dungeoncrawler.wearos.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dungeoncrawler.wearos.domain.model.Equipment

@Entity(tableName = "equipment")
data class EquipmentEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val weaponName: String,
    val armorName: String,
    val attackBonus: Int,
    val defenseBonus: Int,
    val magicBonus: Int,
) {
    companion object {
        const val SINGLETON_ID = 0

        fun default() = Equipment().toEntity()
    }
}

fun EquipmentEntity.toDomain() = Equipment(
    weaponName = weaponName,
    armorName = armorName,
    attackBonus = attackBonus,
    defenseBonus = defenseBonus,
    magicBonus = magicBonus,
)

fun Equipment.toEntity() = EquipmentEntity(
    weaponName = weaponName,
    armorName = armorName,
    attackBonus = attackBonus,
    defenseBonus = defenseBonus,
    magicBonus = magicBonus,
)
