package com.dungeoncrawler.wearos.domain.model

/**
 * A themed 10-floor dungeon. Floors are climbed by walking: every 200 steps resolves a
 * micro-event, every 2000 steps spawns the floor's boss.
 */
data class Dungeon(
    val id: String,
    val name: String,
    val subtitle: String,
    val order: Int,
    val accentColorArgb: Int,
    val difficultyMultiplier: Float,
) {
    companion object {
        const val FLOORS_PER_DUNGEON = 10
    }
}

/** Where the hero currently stands, and which dungeons they have earned access to. */
data class DungeonProgress(
    val currentDungeonId: String,
    val currentFloor: Int = 1,
    val unlockedDungeonIds: Set<String> = setOf(currentDungeonId),
    val clearedDungeonIds: Set<String> = emptySet(),
) {
    val isFinalFloor: Boolean get() = currentFloor >= Dungeon.FLOORS_PER_DUNGEON
    val floorLabel: String get() = "Étage $currentFloor/${Dungeon.FLOORS_PER_DUNGEON}"

    fun isUnlocked(dungeonId: String): Boolean = unlockedDungeonIds.contains(dungeonId)
}

/** The two ways out of a cleared dungeon, offered by `DungeonClearScreen`. */
enum class DungeonClearChoice(val label: String, val description: String) {
    FARM_AGAIN("Refaire le donjon", "Repartir à l'étage 1 pour optimiser l'équipement"),
    NEXT_DUNGEON("Donjon suivant", "Déverrouiller et entrer dans le donjon suivant"),
}
