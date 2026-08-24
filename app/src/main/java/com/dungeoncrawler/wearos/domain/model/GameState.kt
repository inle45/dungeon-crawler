package com.dungeoncrawler.wearos.domain.model

sealed class GameState {
    data class Exploring(val player: PlayerStats) : GameState()
    data class MicroEventResolved(val player: PlayerStats, val event: MicroEvent) : GameState()
    data class BossEncounterTriggered(val player: PlayerStats, val boss: BossEncounter) : GameState()
    data class InCombat(
        val player: PlayerStats,
        val boss: BossEncounter,
        val lastOutcome: CombatOutcome? = null,
    ) : GameState()
    data class Victory(val player: PlayerStats, val floorNumber: Int) : GameState()
    data class Defeat(val floorNumber: Int) : GameState()
}
