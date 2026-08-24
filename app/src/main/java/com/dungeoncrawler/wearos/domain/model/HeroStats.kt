package com.dungeoncrawler.wearos.domain.model

/**
 * The hero's naked stat line plus its run state. There is deliberately no level and no XP:
 * every point of power beyond [BASE] comes from equipped gear (see [HeroPower]).
 */
data class HeroStats(
    val currentHp: Int = BASE.maxHp,
    val base: StatBlock = BASE,
    val totalSteps: Long = 0,
) {
    companion object {
        val BASE = StatBlock(
            maxHp = 100,
            attack = 6,
            defense = 2,
            critRate = 5,
            magicPower = 4,
            damageReduction = 0,
        )
    }
}

/**
 * The hero as the combat system sees it: base line plus everything currently equipped,
 * resolved once by [com.dungeoncrawler.wearos.domain.usecase.ComputeHeroPowerUseCase].
 */
data class HeroPower(
    val currentHp: Int,
    val total: StatBlock,
    val passives: List<ItemPassive>,
    val loadout: Loadout,
) {
    val maxHp: Int get() = total.maxHp
    val isAlive: Boolean get() = currentHp > 0
    val hpRatio: Float get() = if (maxHp == 0) 0f else currentHp.toFloat() / maxHp.toFloat()

    fun hasPassive(passive: ItemPassive): Boolean = passives.contains(passive)

    fun passiveMagnitude(passive: ItemPassive): Float =
        if (hasPassive(passive)) passive.magnitude else 0f
}

/** What is equipped right now, at most one item per slot. */
data class Loadout(val bySlot: Map<EquipmentSlot, EquipmentItem> = emptyMap()) {
    operator fun get(slot: EquipmentSlot): EquipmentItem? = bySlot[slot]

    val equippedItems: List<EquipmentItem> get() = bySlot.values.toList()

    // effectiveStats, not stats: epic and legendary tiers carry their multiplier.
    val totalStats: StatBlock =
        bySlot.values.fold(StatBlock.EMPTY) { acc, item -> acc + item.effectiveStats }

    val passives: List<ItemPassive> =
        bySlot.values.map { it.passive }.filter { it != ItemPassive.NONE }.distinct()

    fun with(item: EquipmentItem): Loadout = Loadout(bySlot + (item.slot to item))

    fun without(slot: EquipmentSlot): Loadout = Loadout(bySlot - slot)
}
