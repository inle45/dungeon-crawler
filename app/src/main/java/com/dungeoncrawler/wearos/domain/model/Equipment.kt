package com.dungeoncrawler.wearos.domain.model

data class Equipment(
    val weaponName: String = "Rusty Sword",
    val armorName: String = "Cloth Tunic",
    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,
    val magicBonus: Int = 0,
)
