package com.dungeoncrawler.wearos.domain.model

/**
 * Unique effects carried by legendary gear (and a couple of epics). Resolved by
 * [com.dungeoncrawler.wearos.domain.usecase.ExecuteCombatActionUseCase] during a turn.
 */
enum class ItemPassive(val displayName: String, val description: String, val magnitude: Float) {
    NONE("—", "Aucun effet passif", 0f),

    /** Heals the hero for a share of the damage dealt. */
    LIFE_STEAL("Vol de vie", "Rend 25 % des dégâts infligés", 0.25f),

    /** On a successful parry, reflects part of the incoming hit back at the monster. */
    RIPOSTE("Riposte", "Renvoie 40 % des dégâts parés", 0.40f),

    /** Ignores a share of the monster's defense before mitigation. */
    ARMOR_PIERCE("Perforation d'armure", "Ignore 35 % de la défense ennemie", 0.35f),

    /** Flat reduction applied to every hit the hero takes. */
    DAMAGE_REDUCTION("Réduction de dégâts", "Réduit les dégâts reçus de 5 %", 0.05f),
}
