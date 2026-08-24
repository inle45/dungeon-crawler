package com.dungeoncrawler.wearos.presentation.combat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.dungeoncrawler.wearos.R
import com.dungeoncrawler.wearos.core.sprite.PixelSpriteAnimation
import com.dungeoncrawler.wearos.core.sprite.rememberSpriteSheet
import com.dungeoncrawler.wearos.core.theme.EmberRed
import com.dungeoncrawler.wearos.core.theme.HealthGreen
import com.dungeoncrawler.wearos.core.theme.OledBlack
import com.dungeoncrawler.wearos.domain.model.CombatAction
import com.dungeoncrawler.wearos.domain.model.CombatOutcome
import com.dungeoncrawler.wearos.presentation.components.ActionButton
import com.dungeoncrawler.wearos.presentation.components.StatBar
import kotlinx.coroutines.flow.collectLatest

@Composable
fun BossCombatScreen(
    onCombatResolved: () -> Unit,
    viewModel: BossCombatViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                BossCombatEffect.CombatEnded -> onCombatResolved()
            }
        }
    }

    RotaryActionSelector(
        onScrollStep = { steps -> viewModel.processIntent(BossCombatIntent.RotateSelection(steps)) },
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = OledBlack)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            CombatArena(state = state)

            Text(
                text = combatOutcomeLabel(state),
                style = MaterialTheme.typography.caption2,
            )

            ActionRow(
                selectedIndex = state.selectedIndex,
                onActionSelected = { index -> viewModel.processIntent(BossCombatIntent.SelectAction(index)) },
                onConfirm = { viewModel.processIntent(BossCombatIntent.ConfirmAction) },
            )
        }
    }
}

@Composable
private fun CombatArena(state: BossCombatState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val heroSprite = rememberSpriteSheet(R.drawable.hero_idle_spritesheet, frameCount = 4)
            PixelSpriteAnimation(spriteSheet = heroSprite, modifier = Modifier.size(56.dp))
            StatBar(
                ratio = state.player.hpRatio,
                color = HealthGreen,
                modifier = Modifier.size(width = 56.dp, height = 4.dp).padding(top = 2.dp),
            )
        }

        Text(text = "VS", style = MaterialTheme.typography.caption3)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val bossSprite = rememberSpriteSheet(R.drawable.boss_idle_spritesheet, frameCount = 4)
            PixelSpriteAnimation(
                spriteSheet = bossSprite,
                modifier = Modifier.size(64.dp),
                mirrored = true,
            )
            StatBar(
                ratio = state.boss.hpRatio,
                color = EmberRed,
                modifier = Modifier.size(width = 64.dp, height = 4.dp).padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun ActionRow(
    selectedIndex: Int,
    onActionSelected: (Int) -> Unit,
    onConfirm: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        COMBAT_ACTIONS.forEachIndexed { index, action ->
            ActionButton(
                icon = painterResource(id = action.iconRes()),
                contentDescription = action.name,
                isSelected = index == selectedIndex,
                onClick = {
                    onActionSelected(index)
                    onConfirm()
                },
            )
        }
    }
}

private fun CombatAction.iconRes(): Int = when (this) {
    CombatAction.ATTACK -> R.drawable.icon_attack_sword
    CombatAction.DEFENSE -> R.drawable.icon_defense_shield
    CombatAction.SPELL -> R.drawable.icon_spell_wand
}

private fun combatOutcomeLabel(state: BossCombatState): String = when (val outcome = state.lastOutcome) {
    null -> "Choose your move"
    is CombatOutcome.PlayerCriticalHit -> "Critical hit! -${outcome.damageDealt}"
    is CombatOutcome.PlayerStandardHit -> "Hit! -${outcome.damageDealt}"
    is CombatOutcome.PlayerSpellCast -> "Spell cast! -${outcome.damageDealt}"
    is CombatOutcome.PlayerParried -> "Parried!"
    is CombatOutcome.PlayerDamaged -> "Ouch! -${outcome.damageTaken}"
    else -> ""
}
