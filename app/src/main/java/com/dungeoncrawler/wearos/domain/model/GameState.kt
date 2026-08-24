package com.dungeoncrawler.wearos.domain.model

/** The single source of truth for where the run stands, observed by every screen. */
sealed class GameState {
    data object Idle : GameState()

    data class Exploring(val progress: DungeonProgress) : GameState()

    data class MicroEventResolved(
        val progress: DungeonProgress,
        val event: MicroEvent,
    ) : GameState()

    data class BossEncounterTriggered(
        val progress: DungeonProgress,
        val monster: Monster,
    ) : GameState()

    data class InCombat(
        val progress: DungeonProgress,
        val monster: Monster,
        val monsterHp: Int,
        val heroHp: Int,
        val lastOutcome: CombatOutcome? = null,
    ) : GameState() {
        val monsterHpRatio: Float
            get() = if (monster.maxHp == 0) 0f else monsterHp.toFloat() / monster.maxHp.toFloat()
    }

    /** A floor boss went down and the hero advanced to the next floor. */
    data class FloorCleared(
        val progress: DungeonProgress,
        val monster: Monster,
        val loot: EquipmentItem?,
    ) : GameState()

    /** Floor 10's supreme boss went down — `DungeonClearScreen` takes over from here. */
    data class DungeonCleared(
        val dungeon: Dungeon,
        val supremeBoss: Monster,
        val loot: EquipmentItem?,
        val nextDungeon: Dungeon?,
    ) : GameState()

    data class Defeat(val progress: DungeonProgress, val monster: Monster) : GameState()
}
