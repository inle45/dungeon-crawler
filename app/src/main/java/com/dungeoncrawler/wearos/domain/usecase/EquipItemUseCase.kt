package com.dungeoncrawler.wearos.domain.usecase

import com.dungeoncrawler.wearos.domain.model.EquipmentItem
import com.dungeoncrawler.wearos.domain.model.HeroPower
import com.dungeoncrawler.wearos.domain.repository.HeroRepository
import com.dungeoncrawler.wearos.domain.repository.InventoryRepository
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Equips gear (or drinks a consumable) and re-derives the hero's stat line. Swapping armor
 * changes max HP, so current HP is rescaled proportionally rather than snapped, keeping a
 * swap from being a free heal or a silent execution.
 */
class EquipItemUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val heroRepository: HeroRepository,
    private val computeHeroPower: ComputeHeroPowerUseCase,
) {
    suspend operator fun invoke(item: EquipmentItem): HeroPower {
        if (item.isConsumable) return consume(item)

        val before = computeHeroPower.once()
        val hpRatio = before.hpRatio

        inventoryRepository.equip(item.id)

        val after = computeHeroPower.once()
        val rescaledHp = (after.maxHp * hpRatio).roundToInt().coerceIn(1, after.maxHp)
        heroRepository.updateHeroStats { it.copy(currentHp = rescaledHp) }

        return after.copy(currentHp = rescaledHp)
    }

    private suspend fun consume(item: EquipmentItem): HeroPower {
        val power = computeHeroPower.once()
        val healed = (power.maxHp * item.healPercent).roundToInt()
        val newHp = (power.currentHp + healed).coerceAtMost(power.maxHp)

        heroRepository.updateHeroStats { it.copy(currentHp = newHp) }
        inventoryRepository.consume(item.id)

        return power.copy(currentHp = newHp)
    }
}
