package com.dungeoncrawler.wearos.domain.usecase

import com.dungeoncrawler.wearos.domain.catalog.EquipmentCatalog
import com.dungeoncrawler.wearos.domain.model.EquipmentItem
import com.dungeoncrawler.wearos.domain.model.Monster
import com.dungeoncrawler.wearos.domain.model.MonsterRole
import com.dungeoncrawler.wearos.domain.repository.InventoryRepository
import javax.inject.Inject
import kotlin.random.Random

/**
 * Rolls a monster's weighted drop table. Each row's [com.dungeoncrawler.wearos.domain.model.LootDrop.weight]
 * is combined with its item's [com.dungeoncrawler.wearos.domain.model.Rarity.lootWeight], so a
 * legendary listed at the same weight as a common still lands far less often.
 */
class RollLootUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository,
) {
    private val random = Random.Default

    suspend operator fun invoke(monster: Monster): EquipmentItem? {
        if (random.nextFloat() > monster.role.dropChance()) return null

        val weighted = monster.dropTable.mapNotNull { drop ->
            val item = EquipmentCatalog.findById(drop.itemId) ?: return@mapNotNull null
            item to drop.weight * item.rarity.lootWeight
        }
        if (weighted.isEmpty()) return null

        val totalWeight = weighted.sumOf { it.second }
        var roll = random.nextInt(totalWeight)
        val item = weighted.first { (_, weight) ->
            roll -= weight
            roll < 0
        }.first

        // A duplicate of gear the hero already owns is dropped silently rather than cluttering
        // the bag — consumables stack, so they always land.
        val isNew = inventoryRepository.addItem(item)
        return if (isNew || item.isConsumable) item else null
    }

    private fun MonsterRole.dropChance(): Float = when (this) {
        MonsterRole.MICRO_MOB -> 0.35f
        MonsterRole.MINI_BOSS -> 0.85f
        MonsterRole.SUPREME_BOSS -> 1.0f
    }
}
