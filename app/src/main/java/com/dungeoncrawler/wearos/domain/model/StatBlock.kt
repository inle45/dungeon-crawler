package com.dungeoncrawler.wearos.domain.model

import kotlin.math.roundToInt

/**
 * Additive bundle of combat stats. Used both for the hero's base line and for each item's
 * contribution, so `TotalStat = BaseHeroStat + Sum(EquippedItems.Stats)` is a plain sum.
 *
 * Every field here is read somewhere in [com.dungeoncrawler.wearos.domain.usecase]: a stat that
 * only decorated a tooltip would make gear feel richer than it plays.
 */
data class StatBlock(
    /** Flat hit points added to the hero's ceiling. */
    val maxHp: Int = 0,
    /** Flat physical damage before the monster's defense is applied. */
    val attack: Int = 0,
    /** Subtracted from incoming damage when defending. */
    val defense: Int = 0,
    /** Percentage points of critical chance, e.g. 8 means +8 %. */
    val critRate: Int = 0,
    /** Percentage points added to the ×1.8 critical multiplier, e.g. 40 makes it ×2.2. */
    val critDamage: Int = 0,
    /** Flat spell damage; spells ignore monster armor entirely. */
    val magicPower: Int = 0,
    /** Percentage points shaved off every hit taken. */
    val damageReduction: Int = 0,
    /** Percentage points of damage dealt returned as healing. */
    val lifeSteal: Int = 0,
    /** Percentage chance to take no damage at all from an incoming hit. */
    val dodge: Int = 0,
    /** Percentage points of the monster's defense ignored. */
    val armorPierce: Int = 0,
    /** Percentage points of damage taken reflected back at the attacker. */
    val thorns: Int = 0,
    /** Percentage points added to a monster's drop chance. */
    val lootBonus: Int = 0,
    /** Flat HP restored after each resolved encounter or combat turn. */
    val hpRegen: Int = 0,
) {
    operator fun plus(other: StatBlock) = StatBlock(
        maxHp = maxHp + other.maxHp,
        attack = attack + other.attack,
        defense = defense + other.defense,
        critRate = critRate + other.critRate,
        critDamage = critDamage + other.critDamage,
        magicPower = magicPower + other.magicPower,
        damageReduction = damageReduction + other.damageReduction,
        lifeSteal = lifeSteal + other.lifeSteal,
        dodge = dodge + other.dodge,
        armorPierce = armorPierce + other.armorPierce,
        thorns = thorns + other.thorns,
        lootBonus = lootBonus + other.lootBonus,
        hpRegen = hpRegen + other.hpRegen,
    )

    operator fun minus(other: StatBlock) = StatBlock(
        maxHp = maxHp - other.maxHp,
        attack = attack - other.attack,
        defense = defense - other.defense,
        critRate = critRate - other.critRate,
        critDamage = critDamage - other.critDamage,
        magicPower = magicPower - other.magicPower,
        damageReduction = damageReduction - other.damageReduction,
        lifeSteal = lifeSteal - other.lifeSteal,
        dodge = dodge - other.dodge,
        armorPierce = armorPierce - other.armorPierce,
        thorns = thorns - other.thorns,
        lootBonus = lootBonus - other.lootBonus,
        hpRegen = hpRegen - other.hpRegen,
    )

    /**
     * Scales the raw stats by an epic/legendary multiplier. Percentage stats are left alone —
     * multiplying dodge or damage reduction stacks toward immunity far too fast.
     */
    operator fun times(multiplier: Float) = copy(
        maxHp = (maxHp * multiplier).roundToInt(),
        attack = (attack * multiplier).roundToInt(),
        defense = (defense * multiplier).roundToInt(),
        magicPower = (magicPower * multiplier).roundToInt(),
        hpRegen = (hpRegen * multiplier).roundToInt(),
    )

    val isEmpty: Boolean get() = this == EMPTY

    /** Non-zero stats in display order, for the inventory comparator. */
    fun namedEntries(): List<Pair<String, Int>> = listOf(
        "PV" to maxHp,
        "ATQ" to attack,
        "DEF" to defense,
        "CRIT" to critRate,
        "DCRIT" to critDamage,
        "MAG" to magicPower,
        "RED" to damageReduction,
        "VOL" to lifeSteal,
        "ESQ" to dodge,
        "PERF" to armorPierce,
        "ÉPIN" to thorns,
        "BUTIN" to lootBonus,
        "RÉGÉ" to hpRegen,
    ).filter { it.second != 0 }

    /** How many stats this block touches — used to check an item against its rarity contract. */
    val filledStatCount: Int get() = namedEntries().size

    companion object {
        val EMPTY = StatBlock()

        /** Base critical multiplier before any [critDamage] is added. */
        const val BASE_CRIT_MULTIPLIER = 1.8f
    }
}
