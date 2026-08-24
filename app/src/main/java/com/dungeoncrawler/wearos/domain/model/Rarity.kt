package com.dungeoncrawler.wearos.domain.model

/**
 * Five-tier rarity ladder. Each step up adds one more secondary bonus; the top two tiers also
 * carry a stat multiplier and, for [LEGENDARY], a unique [ItemPassive].
 */
enum class Rarity(
    val displayName: String,
    val colorArgb: Int,
    val secondaryBonusCount: Int,
    val statMultiplier: Float,
    val lootWeight: Int,
) {
    COMMON("Commun", 0xFFBDBDBD.toInt(), secondaryBonusCount = 0, statMultiplier = 1.0f, lootWeight = 50),
    UNCOMMON("Peu commun", 0xFF4CAF50.toInt(), secondaryBonusCount = 1, statMultiplier = 1.0f, lootWeight = 27),
    RARE("Rare", 0xFF3F8CFF.toInt(), secondaryBonusCount = 2, statMultiplier = 1.0f, lootWeight = 15),
    EPIC("Épique", 0xFF9C4DFF.toInt(), secondaryBonusCount = 3, statMultiplier = 1.15f, lootWeight = 6),
    LEGENDARY("Légendaire", 0xFFE0B84C.toInt(), secondaryBonusCount = 3, statMultiplier = 1.30f, lootWeight = 2),
}
