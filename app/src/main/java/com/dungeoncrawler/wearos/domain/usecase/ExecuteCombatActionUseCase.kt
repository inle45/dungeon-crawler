package com.dungeoncrawler.wearos.domain.usecase

import com.dungeoncrawler.wearos.domain.model.BossEncounter
import com.dungeoncrawler.wearos.domain.model.CombatAction
import com.dungeoncrawler.wearos.domain.model.CombatOutcome
import com.dungeoncrawler.wearos.domain.model.GameState
import com.dungeoncrawler.wearos.domain.repository.GameProgressRepository
import com.dungeoncrawler.wearos.domain.repository.PlayerRepository
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.flow.first

/** Resolves one player action in an active [GameState.InCombat] turn and advances combat state. */
class ExecuteCombatActionUseCase @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val gameProgressRepository: GameProgressRepository,
) {
    suspend operator fun invoke(action: CombatAction, boss: BossEncounter): CombatOutcome {
        val player = playerRepository.observePlayerStats().first()
        val equipment = playerRepository.observeEquipment().first()

        var updatedBoss = boss
        var updatedPlayer = player
        val outcome: CombatOutcome

        when (action) {
            CombatAction.ATTACK -> {
                val isCritical = Random.nextFloat() < 0.2f
                val rawDamage = player.effectiveAttack(equipment)
                val damage = if (isCritical) (rawDamage * 1.8f).toInt() else rawDamage
                updatedBoss = boss.copy(currentHp = (boss.currentHp - damage).coerceAtLeast(0))
                outcome = if (isCritical) {
                    CombatOutcome.PlayerCriticalHit(damage)
                } else {
                    CombatOutcome.PlayerStandardHit(damage)
                }
            }
            CombatAction.SPELL -> {
                val damage = player.effectiveMagicPower(equipment) + Random.nextInt(0, 5)
                updatedBoss = boss.copy(currentHp = (boss.currentHp - damage).coerceAtLeast(0))
                outcome = CombatOutcome.PlayerSpellCast(damage)
            }
            CombatAction.DEFENSE -> {
                val isParried = Random.nextFloat() < 0.4f
                val bossDamage = 8 + boss.floorNumber * 2
                if (isParried) {
                    outcome = CombatOutcome.PlayerParried(bossDamage)
                } else {
                    val mitigated = (bossDamage - player.effectiveDefense(equipment)).coerceAtLeast(1)
                    updatedPlayer = player.copy(currentHp = (player.currentHp - mitigated).coerceAtLeast(0))
                    outcome = CombatOutcome.PlayerDamaged(mitigated)
                }
            }
        }

        playerRepository.updatePlayerStats { updatedPlayer }

        val finalOutcome = when {
            updatedBoss.isDefeated -> CombatOutcome.BossDefeated(boss.floorNumber)
            !updatedPlayer.isAlive -> CombatOutcome.PlayerDefeated
            else -> outcome
        }

        val nextState = when (finalOutcome) {
            is CombatOutcome.BossDefeated -> GameState.Victory(updatedPlayer, boss.floorNumber)
            is CombatOutcome.PlayerDefeated -> GameState.Defeat(boss.floorNumber)
            else -> GameState.InCombat(updatedPlayer, updatedBoss, outcome)
        }
        gameProgressRepository.setGameState(nextState)

        return finalOutcome
    }
}
