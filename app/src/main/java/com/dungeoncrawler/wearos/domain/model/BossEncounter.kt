package com.dungeoncrawler.wearos.domain.model

data class BossEncounter(
    val bossName: String,
    val maxHp: Int,
    val currentHp: Int,
    val floorNumber: Int,
) {
    val hpRatio: Float get() = currentHp.toFloat() / maxHp.toFloat()
    val isDefeated: Boolean get() = currentHp <= 0

    companion object {
        fun forFloor(floorNumber: Int): BossEncounter {
            val hp = 60 + floorNumber * 20
            return BossEncounter(
                bossName = "Floor $floorNumber Guardian",
                maxHp = hp,
                currentHp = hp,
                floorNumber = floorNumber,
            )
        }
    }
}
