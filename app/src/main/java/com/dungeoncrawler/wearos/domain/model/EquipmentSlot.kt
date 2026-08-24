package com.dungeoncrawler.wearos.domain.model

/** One item may be equipped per slot; equipping a second one in the same slot swaps it out. */
enum class EquipmentSlot(val displayName: String) {
    WEAPON("Arme"),
    ARMOR("Armure"),
    RING("Anneau"),
    RELIC("Relique"),
    CONSUMABLE("Consommable"),
}
