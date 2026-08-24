package com.dungeoncrawler.wearos.domain.model

enum class CombatAction(val label: String) {
    ATTACK("Attaque"),
    DEFENSE("Défense"),
    SPELL("Sort"),
}

sealed class CombatOutcome {
    data class PlayerCriticalHit(val damageDealt: Int, val lifeStolen: Int = 0) : CombatOutcome()
    data class PlayerStandardHit(val damageDealt: Int, val lifeStolen: Int = 0) : CombatOutcome()
    data class PlayerSpellCast(val damageDealt: Int) : CombatOutcome()
    data class PlayerParried(val damageMitigated: Int, val riposteDamage: Int = 0) : CombatOutcome()
    data class PlayerDamaged(val damageTaken: Int) : CombatOutcome()
    data class MonsterSlain(val monster: Monster, val loot: EquipmentItem?) : CombatOutcome()
    data class PlayerDefeated(val monster: Monster) : CombatOutcome()
}
