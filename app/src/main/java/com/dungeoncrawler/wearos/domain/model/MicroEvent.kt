package com.dungeoncrawler.wearos.domain.model

/** Passive event auto-resolved every [com.dungeoncrawler.wearos.domain.GameConstants.STEPS_PER_MICRO_EVENT] steps. */
sealed class MicroEvent {
    data class Loot(val itemName: String, val attackBonus: Int, val defenseBonus: Int) : MicroEvent()
    data class Trap(val damage: Int) : MicroEvent()
    data class MicroMob(val xpGained: Int, val damageTaken: Int) : MicroEvent()
}
