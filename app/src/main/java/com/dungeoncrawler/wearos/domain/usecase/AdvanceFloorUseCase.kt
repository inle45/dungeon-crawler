package com.dungeoncrawler.wearos.domain.usecase

import com.dungeoncrawler.wearos.domain.catalog.DungeonCatalog
import com.dungeoncrawler.wearos.domain.model.Dungeon
import com.dungeoncrawler.wearos.domain.model.DungeonClearChoice
import com.dungeoncrawler.wearos.domain.model.DungeonProgress
import com.dungeoncrawler.wearos.domain.repository.HeroRepository
import javax.inject.Inject

/**
 * Owns every legal floor transition. Floors only ever move forward one at a time, and floor 10
 * does not roll over on its own — clearing it hands control to `DungeonClearScreen`, which comes
 * back through [applyClearChoice].
 */
class AdvanceFloorUseCase @Inject constructor(
    private val heroRepository: HeroRepository,
) {
    /** Advances one floor after a mini-boss dies. Returns false on floor 10 (dungeon cleared). */
    suspend fun advance(): Boolean {
        val progress = heroRepository.getDungeonProgress()
        if (progress.isFinalFloor) return false

        heroRepository.updateDungeonProgress { it.copy(currentFloor = it.currentFloor + 1) }
        return true
    }

    /** Marks the current dungeon cleared and unlocks the next one, without moving the hero yet. */
    suspend fun markCleared(): Dungeon? {
        val progress = heroRepository.getDungeonProgress()
        val next = DungeonCatalog.nextAfter(progress.currentDungeonId)

        heroRepository.updateDungeonProgress { current ->
            current.copy(
                clearedDungeonIds = current.clearedDungeonIds + current.currentDungeonId,
                unlockedDungeonIds = current.unlockedDungeonIds + listOfNotNull(next?.id),
            )
        }
        return next
    }

    /** Applies the player's pick on `DungeonClearScreen`, resetting the run to floor 1 either way. */
    suspend fun applyClearChoice(choice: DungeonClearChoice): DungeonProgress {
        heroRepository.updateDungeonProgress { progress ->
            val targetDungeonId = when (choice) {
                DungeonClearChoice.FARM_AGAIN -> progress.currentDungeonId
                DungeonClearChoice.NEXT_DUNGEON ->
                    DungeonCatalog.nextAfter(progress.currentDungeonId)?.id
                        ?.takeIf { progress.isUnlocked(it) }
                        ?: progress.currentDungeonId
            }
            progress.copy(currentDungeonId = targetDungeonId, currentFloor = 1)
        }
        return heroRepository.getDungeonProgress()
    }
}
