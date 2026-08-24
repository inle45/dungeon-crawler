package com.dungeoncrawler.wearos.domain.model

data class PlayerStats(
    val level: Int = 1,
    val maxHp: Int = 100,
    val currentHp: Int = 100,
    val baseAttack: Int = 12,
    val baseDefense: Int = 8,
    val baseMagicPower: Int = 10,
    val xp: Int = 0,
    val totalSteps: Long = 0,
    val currentFloor: Int = 1,
) {
    val isAlive: Boolean get() = currentHp > 0
    val hpRatio: Float get() = currentHp.toFloat() / maxHp.toFloat()

    fun effectiveAttack(equipment: Equipment) = baseAttack + equipment.attackBonus
    fun effectiveDefense(equipment: Equipment) = baseDefense + equipment.defenseBonus
    fun effectiveMagicPower(equipment: Equipment) = baseMagicPower + equipment.magicBonus
}
