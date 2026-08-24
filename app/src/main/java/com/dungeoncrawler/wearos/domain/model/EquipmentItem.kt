package com.dungeoncrawler.wearos.domain.model

/**
 * A lootable piece of gear. All hero power comes from these — there are no levels and no XP.
 *
 * @param stats the raw contribution added to the hero's base line when equipped. Epic and
 *   legendary entries are scaled by [Rarity.statMultiplier] at construction.
 * @param healPercent for [EquipmentSlot.CONSUMABLE] only: share of max HP restored on use.
 * @param dungeonId the dungeon this piece drops in, or null for the base catalog that drops
 *   everywhere.
 * @param family groups the rarity variants of one piece ("Lame d'ossement" and its sharpened and
 *   runic versions), so the inventory can show a ladder instead of unrelated names.
 */
data class EquipmentItem(
    val id: String,
    val name: String,
    val slot: EquipmentSlot,
    val rarity: Rarity,
    val stats: StatBlock,
    val passive: ItemPassive = ItemPassive.NONE,
    val healPercent: Float = 0f,
    val iconRes: String = slot.defaultIconName(),
    val dungeonId: String? = null,
    val family: String = id,
) {
    val isConsumable: Boolean get() = slot == EquipmentSlot.CONSUMABLE

    /** Stats as actually applied: epic and legendary tiers carry their multiplier baked in. */
    val effectiveStats: StatBlock = stats * rarity.statMultiplier

    /** Single-figure score used to sort the inventory and hint at an upgrade at a glance. */
    val powerScore: Int = with(effectiveStats) {
        attack * 3 + defense * 3 + magicPower * 2 + maxHp / 4 +
            critRate * 2 + critDamage + damageReduction * 4 + lifeSteal * 3 +
            dodge * 3 + armorPierce * 2 + thorns * 2 + lootBonus + hpRegen * 4 +
            if (passive == ItemPassive.NONE) 0 else 20
    }
}

private fun EquipmentSlot.defaultIconName(): String = when (this) {
    EquipmentSlot.WEAPON -> "icon_attack_sword"
    EquipmentSlot.ARMOR -> "icon_item_armor"
    EquipmentSlot.RING -> "icon_item_ring"
    EquipmentSlot.RELIC -> "icon_item_relic"
    EquipmentSlot.CONSUMABLE -> "icon_item_potion"
}
