package com.dungeoncrawler.wearos.domain.model

/**
 * A lootable piece of gear. All hero power comes from these — there are no levels and no XP.
 *
 * @param stats the raw contribution added to the hero's base line when equipped. Epic and
 *   legendary entries already bake in [Rarity.statMultiplier]; see [EquipmentCatalog].
 * @param healPercent for [EquipmentSlot.CONSUMABLE] only: share of max HP restored on use.
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
) {
    val isConsumable: Boolean get() = slot == EquipmentSlot.CONSUMABLE

    /** Single-figure score used to sort the inventory and to hint at an upgrade at a glance. */
    val powerScore: Int =
        stats.attack * 3 + stats.defense * 3 + stats.magicPower * 2 +
            stats.critRate * 2 + stats.maxHp / 4 + stats.damageReduction * 4 +
            if (passive == ItemPassive.NONE) 0 else 20
}

private fun EquipmentSlot.defaultIconName(): String = when (this) {
    EquipmentSlot.WEAPON -> "icon_attack_sword"
    EquipmentSlot.ARMOR -> "icon_item_armor"
    EquipmentSlot.RING -> "icon_item_ring"
    EquipmentSlot.RELIC -> "icon_item_relic"
    EquipmentSlot.CONSUMABLE -> "icon_item_potion"
}
