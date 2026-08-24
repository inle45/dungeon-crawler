package com.dungeoncrawler.wearos.domain.catalog

import com.dungeoncrawler.wearos.domain.model.EquipmentItem
import com.dungeoncrawler.wearos.domain.model.Rarity

/**
 * The single lookup over every piece of gear in the game: the base catalog that drops anywhere
 * plus each dungeon's own 20-piece pool. Drop tables store ids, so this is what turns a rolled
 * id back into an item.
 */
object GearIndex {

    val ALL: List<EquipmentItem> = EquipmentCatalog.ALL + DungeonGearCatalog.ALL

    private val byId: Map<String, EquipmentItem> = ALL.associateBy { it.id }

    init {
        // A duplicate id would silently shadow one item and make its drops unreachable.
        require(byId.size == ALL.size) {
            val dupes = ALL.groupBy { it.id }.filterValues { it.size > 1 }.keys
            "Duplicate equipment ids: $dupes"
        }
    }

    fun findById(id: String): EquipmentItem? = byId[id]

    fun ofRarity(rarity: Rarity): List<EquipmentItem> = ALL.filter { it.rarity == rarity }

    /** Every rarity variant of one family, weakest first — the ladder shown in the inventory. */
    fun family(family: String): List<EquipmentItem> =
        ALL.filter { it.family == family }.sortedBy { it.rarity.ordinal }
}
