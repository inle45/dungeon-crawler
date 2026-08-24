package com.dungeoncrawler.wearos.domain.model

enum class CombatAction {
    ATTACK,
    DEFENSE,
    SPELL,
}

sealed class CombatOutcome {
    data class PlayerCriticalHit(val damageDealt: Int) : CombatOutcome()
    data class PlayerStandardHit(val damageDealt: Int) : CombatOutcome()
    data class PlayerSpellCast(val damageDealt: Int) : CombatOutcome()
    data class PlayerParried(val damageMitigated: Int) : CombatOutcome()
    data class PlayerDamaged(val damageTaken: Int) : CombatOutcome()
    data class BossDefeated(val floorNumber: Int) : CombatOutcome()
    data object PlayerDefeated : CombatOutcome()
}
