package com.dungeoncrawler.wearos.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dungeoncrawler.wearos.domain.model.EquipmentItem
import com.dungeoncrawler.wearos.domain.model.EquipmentSlot
import com.dungeoncrawler.wearos.domain.model.ItemPassive
import com.dungeoncrawler.wearos.domain.model.Rarity
import com.dungeoncrawler.wearos.domain.model.StatBlock

/**
 * One owned piece of gear. [isEquipped] is what makes it count toward the hero's total stats.
 *
 * Bonuses are stored as written in the catalog — the rarity multiplier is applied on read via
 * [EquipmentItem.effectiveStats], so re-tuning a multiplier does not need a data migration.
 */
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
    val bonusCritDamage: Int,
    val bonusMagicPower: Int,
    val bonusDamageReduction: Int,
    val bonusLifeSteal: Int,
    val bonusDodge: Int,
    val bonusArmorPierce: Int,
    val bonusThorns: Int,
    val bonusLootBonus: Int,
    val bonusHpRegen: Int,
    val passive: String,
    val healPercent: Float,
    val iconRes: String,
    val dungeonId: String?,
    val family: String,
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
        critDamage = bonusCritDamage,
        magicPower = bonusMagicPower,
        damageReduction = bonusDamageReduction,
        lifeSteal = bonusLifeSteal,
        dodge = bonusDodge,
        armorPierce = bonusArmorPierce,
        thorns = bonusThorns,
        lootBonus = bonusLootBonus,
        hpRegen = bonusHpRegen,
    ),
    passive = ItemPassive.valueOf(passive),
    healPercent = healPercent,
    iconRes = iconRes,
    dungeonId = dungeonId,
    family = family,
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
    bonusCritDamage = stats.critDamage,
    bonusMagicPower = stats.magicPower,
    bonusDamageReduction = stats.damageReduction,
    bonusLifeSteal = stats.lifeSteal,
    bonusDodge = stats.dodge,
    bonusArmorPierce = stats.armorPierce,
    bonusThorns = stats.thorns,
    bonusLootBonus = stats.lootBonus,
    bonusHpRegen = stats.hpRegen,
    passive = passive.name,
    healPercent = healPercent,
    iconRes = iconRes,
    dungeonId = dungeonId,
    family = family,
    isEquipped = isEquipped,
)
