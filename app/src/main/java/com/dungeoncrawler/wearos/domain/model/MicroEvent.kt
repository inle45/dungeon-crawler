package com.dungeoncrawler.wearos.domain.model

/** Passive event auto-resolved every [com.dungeoncrawler.wearos.domain.GameConstants.STEPS_PER_MICRO_EVENT] steps. */
sealed class MicroEvent {
    /** A chest or corpse yielded gear. */
    data class Loot(val item: EquipmentItem) : MicroEvent()

    /** A trap the hero walked into. */
    data class Trap(val damage: Int) : MicroEvent()

    /** A micro-mob resolved off-screen: the hero always wins, but may take a scratch. */
    data class MicroMobSlain(
        val monster: Monster,
        val damageTaken: Int,
        val loot: EquipmentItem?,
    ) : MicroEvent()
}
