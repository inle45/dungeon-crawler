package com.dungeoncrawler.wearos.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dungeoncrawler.wearos.domain.model.EquipmentItem
import com.dungeoncrawler.wearos.domain.model.EquipmentSlot
import com.dungeoncrawler.wearos.domain.model.ItemPassive
import com.dungeoncrawler.wearos.domain.model.Rarity
import com.dungeoncrawler.wearos.domain.model.StatBlock

/** One owned piece of gear. [isEquipped] is what makes it count toward the hero's total stats. */
@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val slot: String,
    val rarity: String,
    val bonusMaxHp: Int,
    val bonusAttack: Int,
    val bonusDefense: Int,
    val bonusCritRate: Int,
    val bonusMagicPower: Int,
    val bonusDamageReduction: Int,
    val passive: String,
    val healPercent: Float,
    val iconRes: String,
    val isEquipped: Boolean = false,
)

fun InventoryItemEntity.toDomain() = EquipmentItem(
    id = id,
    name = name,
    slot = EquipmentSlot.valueOf(slot),
    rarity = Rarity.valueOf(rarity),
    stats = StatBlock(
        maxHp = bonusMaxHp,
        attack = bonusAttack,
        defense = bonusDefense,
        critRate = bonusCritRate,
        magicPower = bonusMagicPower,
        damageReduction = bonusDamageReduction,
    ),
    passive = ItemPassive.valueOf(passive),
    healPercent = healPercent,
    iconRes = iconRes,
)

fun EquipmentItem.toEntity(isEquipped: Boolean = false) = InventoryItemEntity(
    id = id,
    name = name,
    slot = slot.name,
    rarity = rarity.name,
    bonusMaxHp = stats.maxHp,
    bonusAttack = stats.attack,
    bonusDefense = stats.defense,
    bonusCritRate = stats.critRate,
    bonusMagicPower = stats.magicPower,
    bonusDamageReduction = stats.damageReduction,
    passive = passive.name,
    healPercent = healPercent,
    iconRes = iconRes,
    isEquipped = isEquipped,
)
