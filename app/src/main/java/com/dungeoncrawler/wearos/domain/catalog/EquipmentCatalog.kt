package com.dungeoncrawler.wearos.domain.catalog

import com.dungeoncrawler.wearos.domain.model.EquipmentItem
import com.dungeoncrawler.wearos.domain.model.EquipmentSlot
import com.dungeoncrawler.wearos.domain.model.ItemPassive
import com.dungeoncrawler.wearos.domain.model.Rarity
import com.dungeoncrawler.wearos.domain.model.StatBlock

/**
 * The starting gear catalog, seeded into Room on first launch. Every drop table in
 * [DungeonCatalog] pulls from these ids, so this object is the single source of truth for what
 * an item is worth.
 */
object EquipmentCatalog {

    val RUSTY_SWORD = EquipmentItem(
        id = "wpn_rusty_sword",
        name = "Épée rouillée",
        slot = EquipmentSlot.WEAPON,
        rarity = Rarity.COMMON,
        stats = StatBlock(attack = 5),
    )

    val SHADOW_DAGGER = EquipmentItem(
        id = "wpn_shadow_dagger",
        name = "Dague d'ombre",
        slot = EquipmentSlot.WEAPON,
        rarity = Rarity.RARE,
        stats = StatBlock(attack = 12, critRate = 8),
    )

    val ANCESTRAL_RUNEBLADE = EquipmentItem(
        id = "wpn_ancestral_runeblade",
        name = "Lame runique ancestrale",
        slot = EquipmentSlot.WEAPON,
        rarity = Rarity.LEGENDARY,
        stats = StatBlock(attack = 28, critRate = 15),
        passive = ItemPassive.LIFE_STEAL,
    )

    val WORN_LEATHER_TUNIC = EquipmentItem(
        id = "arm_worn_leather_tunic",
        name = "Tunique de cuir usée",
        slot = EquipmentSlot.ARMOR,
        rarity = Rarity.COMMON,
        stats = StatBlock(maxHp = 30, defense = 3),
    )

    val FORGED_PLATE_CUIRASS = EquipmentItem(
        id = "arm_forged_plate_cuirass",
        name = "Plastron de plates forgé",
        slot = EquipmentSlot.ARMOR,
        rarity = Rarity.UNCOMMON,
        stats = StatBlock(maxHp = 70, defense = 8),
    )

    val ABYSSAL_TITAN_ARMOR = EquipmentItem(
        id = "arm_abyssal_titan",
        name = "Armure du titan abyssal",
        slot = EquipmentSlot.ARMOR,
        rarity = Rarity.EPIC,
        stats = StatBlock(maxHp = 180, defense = 22, damageReduction = 5),
        passive = ItemPassive.DAMAGE_REDUCTION,
    )

    val RING_OF_VITALITY = EquipmentItem(
        id = "rng_vitality",
        name = "Anneau de vitalité",
        slot = EquipmentSlot.RING,
        rarity = Rarity.UNCOMMON,
        stats = StatBlock(maxHp = 50),
    )

    val SLAYERS_SIGNET = EquipmentItem(
        id = "rng_slayers_signet",
        name = "Chevalière du massacreur",
        slot = EquipmentSlot.RING,
        rarity = Rarity.EPIC,
        stats = StatBlock(attack = 6, critRate = 10),
        passive = ItemPassive.ARMOR_PIERCE,
    )

    val ABYSS_AMULET = EquipmentItem(
        id = "rlc_abyss_amulet",
        name = "Amulette des abysses",
        slot = EquipmentSlot.RELIC,
        rarity = Rarity.RARE,
        stats = StatBlock(defense = 5, magicPower = 15),
    )

    val MAJOR_HEALING_POTION = EquipmentItem(
        id = "cns_major_healing_potion",
        name = "Potion de soin majeure",
        slot = EquipmentSlot.CONSUMABLE,
        rarity = Rarity.COMMON,
        stats = StatBlock.EMPTY,
        healPercent = 0.50f,
    )

    val ALL: List<EquipmentItem> = listOf(
        RUSTY_SWORD,
        SHADOW_DAGGER,
        ANCESTRAL_RUNEBLADE,
        WORN_LEATHER_TUNIC,
        FORGED_PLATE_CUIRASS,
        ABYSSAL_TITAN_ARMOR,
        RING_OF_VITALITY,
        SLAYERS_SIGNET,
        ABYSS_AMULET,
        MAJOR_HEALING_POTION,
    )

    private val byId: Map<String, EquipmentItem> = ALL.associateBy { it.id }

    fun findById(id: String): EquipmentItem? = byId[id]

    fun ofRarity(rarity: Rarity): List<EquipmentItem> = ALL.filter { it.rarity == rarity }

    /** Gear the hero starts a fresh save with — the weakest weapon and armor, nothing else. */
    val STARTER_LOADOUT_IDS: List<String> = listOf(RUSTY_SWORD.id, WORN_LEATHER_TUNIC.id)
}
