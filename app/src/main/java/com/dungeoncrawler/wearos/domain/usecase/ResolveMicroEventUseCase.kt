package com.dungeoncrawler.wearos.domain.usecase

import com.dungeoncrawler.wearos.domain.model.GameState
import com.dungeoncrawler.wearos.domain.model.MicroEvent
import com.dungeoncrawler.wearos.domain.model.PlayerStats
import com.dungeoncrawler.wearos.domain.repository.GameProgressRepository
import com.dungeoncrawler.wearos.domain.repository.PlayerRepository
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.flow.first

/** Rolls and applies one passive micro-event (loot / trap / micro-mob), persisting the result. */
class ResolveMicroEventUseCase @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val gameProgressRepository: GameProgressRepository,
) {
    suspend operator fun invoke(): MicroEvent {
        val event = roll()

        var updatedStats: PlayerStats = PlayerStats()
        when (event) {
            is MicroEvent.Loot -> {
                playerRepository.updateEquipment { equipment ->
                    equipment.copy(
                        attackBonus = equipment.attackBonus + event.attackBonus,
                        defenseBonus = equipment.defenseBonus + event.defenseBonus,
                        weaponName = if (event.attackBonus > 0) event.itemName else equipment.weaponName,
                        armorName = if (event.defenseBonus > 0) event.itemName else equipment.armorName,
                    )
                }
                updatedStats = playerRepository.observePlayerStats().first()
            }
            is MicroEvent.Trap -> {
                playerRepository.updatePlayerStats { stats ->
                    stats.copy(currentHp = (stats.currentHp - event.damage).coerceAtLeast(0))
                        .also { updatedStats = it }
                }
            }
            is MicroEvent.MicroMob -> {
                playerRepository.updatePlayerStats { stats ->
                    stats.copy(
                        currentHp = (stats.currentHp - event.damageTaken).coerceAtLeast(0),
                        xp = stats.xp + event.xpGained,
                    ).also { updatedStats = it }
                }
            }
        }

        gameProgressRepository.setGameState(GameState.MicroEventResolved(updatedStats, event))
        return event
    }

    private fun roll(): MicroEvent = when (Random.nextInt(3)) {
        0 -> MicroEvent.Loot(
            itemName = LOOT_NAMES.random(),
            attackBonus = if (Random.nextBoolean()) Random.nextInt(1, 4) else 0,
            defenseBonus = if (Random.nextBoolean()) Random.nextInt(1, 3) else 0,
        )
        1 -> MicroEvent.Trap(damage = Random.nextInt(3, 10))
        else -> MicroEvent.MicroMob(
            xpGained = Random.nextInt(5, 15),
            damageTaken = Random.nextInt(0, 6),
        )
    }

    private companion object {
        val LOOT_NAMES = listOf("Iron Blade", "Oak Buckler", "Silk Sash", "Bone Charm")
    }
}
