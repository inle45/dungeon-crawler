package com.dungeoncrawler.wearos.domain.model

import kotlin.math.roundToInt

/**
 * Additive bundle of combat stats. Used both for the hero's base line and for each item's
 * contribution, so `TotalStat = BaseHeroStat + Sum(EquippedItems.Stats)` is a plain sum.
 */
data class StatBlock(
    val maxHp: Int = 0,
    val attack: Int = 0,
    val defense: Int = 0,
    /** Percentage points, e.g. 8 means +8 % critical chance. */
    val critRate: Int = 0,
    val magicPower: Int = 0,
    /** Percentage points of incoming damage shaved off, e.g. 5 means -5 % damage taken. */
    val damageReduction: Int = 0,
) {
    operator fun plus(other: StatBlock) = StatBlock(
        maxHp = maxHp + other.maxHp,
        attack = attack + other.attack,
        defense = defense + other.defense,
        critRate = critRate + other.critRate,
        magicPower = magicPower + other.magicPower,
        damageReduction = damageReduction + other.damageReduction,
    )

    operator fun minus(other: StatBlock) = StatBlock(
        maxHp = maxHp - other.maxHp,
        attack = attack - other.attack,
        defense = defense - other.defense,
        critRate = critRate - other.critRate,
        magicPower = magicPower - other.magicPower,
        damageReduction = damageReduction - other.damageReduction,
    )

    operator fun times(multiplier: Float) = StatBlock(
        maxHp = (maxHp * multiplier).roundToInt(),
        attack = (attack * multiplier).roundToInt(),
        defense = (defense * multiplier).roundToInt(),
        critRate = critRate,
        magicPower = (magicPower * multiplier).roundToInt(),
        damageReduction = damageReduction,
    )

    val isEmpty: Boolean get() = this == EMPTY

    companion object {
        val EMPTY = StatBlock()
    }
}
