package com.dungeoncrawler.wearos.domain.usecase

import com.dungeoncrawler.wearos.domain.model.EquipmentItem
import com.dungeoncrawler.wearos.domain.model.HeroPower
import com.dungeoncrawler.wearos.domain.model.HeroStats
import com.dungeoncrawler.wearos.domain.model.Loadout
import com.dungeoncrawler.wearos.domain.repository.HeroRepository
import com.dungeoncrawler.wearos.domain.repository.InventoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Resolves `TotalStat = BaseHeroStat + Sum(EquippedItems.Stats)` — the only place hero power is
 * computed, so equipping anything anywhere flows through here.
 */
class ComputeHeroPowerUseCase @Inject constructor(
    private val heroRepository: HeroRepository,
    private val inventoryRepository: InventoryRepository,
) {
    operator fun invoke(): Flow<HeroPower> =
        combine(
            heroRepository.observeHeroStats(),
            inventoryRepository.observeLoadout(),
        ) { hero, loadout -> resolve(hero, loadout) }

    suspend fun once(): HeroPower =
        resolve(heroRepository.getHeroStats(), inventoryRepository.getLoadout())

    /** Previews the hero's stat line as it would be with [candidate] equipped, for the comparator. */
    suspend fun preview(candidate: EquipmentItem): HeroPower {
        val hero = heroRepository.getHeroStats()
        val loadout = inventoryRepository.getLoadout().with(candidate)
        return resolve(hero, loadout)
    }

    private fun resolve(hero: HeroStats, loadout: Loadout): HeroPower {
        val total = hero.base + loadout.totalStats
        return HeroPower(
            // Equipping HP gear must never leave the hero above their new ceiling.
            currentHp = hero.currentHp.coerceIn(0, total.maxHp),
            total = total,
            passives = loadout.passives,
            loadout = loadout,
        )
    }
}
