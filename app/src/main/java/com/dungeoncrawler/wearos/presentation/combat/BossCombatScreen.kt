package com.dungeoncrawler.wearos.presentation.combat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.dungeoncrawler.wearos.R
import com.dungeoncrawler.wearos.core.sprite.PixelSpriteAnimation
import com.dungeoncrawler.wearos.core.sprite.rememberSpriteSheet
import com.dungeoncrawler.wearos.core.theme.EmberRed
import com.dungeoncrawler.wearos.core.theme.GoldAccent
import com.dungeoncrawler.wearos.core.theme.HealthGreen
import com.dungeoncrawler.wearos.core.theme.OledBlack
import com.dungeoncrawler.wearos.core.theme.TextSecondary
import com.dungeoncrawler.wearos.domain.GameConstants
import com.dungeoncrawler.wearos.domain.model.CombatAction
import com.dungeoncrawler.wearos.domain.model.CombatOutcome
import com.dungeoncrawler.wearos.presentation.components.ActionButton
import com.dungeoncrawler.wearos.presentation.components.RotarySelector
import com.dungeoncrawler.wearos.presentation.components.StatBar
import com.dungeoncrawler.wearos.presentation.components.rememberDrawableId
import kotlinx.coroutines.flow.collectLatest

@Composable
fun BossCombatScreen(
    onFloorCleared: () -> Unit,
    onDungeonCleared: () -> Unit,
    onDefeated: () -> Unit,
    viewModel: BossCombatViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                BossCombatEffect.FloorCleared -> onFloorCleared()
                BossCombatEffect.DungeonCleared -> onDungeonCleared()
                BossCombatEffect.Defeated -> onDefeated()
            }
        }
    }

    RotarySelector(
        onScrollStep = { steps -> viewModel.processIntent(BossCombatIntent.RotateSelection(steps)) },
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OledBlack)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.floorLabel,
                    style = MaterialTheme.typography.caption3,
                    color = TextSecondary,
                )
                state.monster?.let { monster ->
                    Text(
                        text = monster.name,
                        style = MaterialTheme.typography.caption2,
                        color = GoldAccent,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }

            CombatArena(state = state)

            Text(
                text = combatOutcomeLabel(state.lastOutcome),
                style = MaterialTheme.typography.caption3,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )

            ActionRow(
                selectedIndex = state.selectedIndex,
                enabled = !state.isResolving,
                onActionSelected = { viewModel.processIntent(BossCombatIntent.SelectAction(it)) },
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
            val heroSprite = rememberSpriteSheet(
                R.drawable.hero_idle_spritesheet,
                frameCount = GameConstants.SPRITE_FRAME_COUNT,
            )
            PixelSpriteAnimation(spriteSheet = heroSprite, modifier = Modifier.size(52.dp))
            StatBar(
                ratio = state.power?.hpRatio ?: 0f,
                color = HealthGreen,
                modifier = Modifier
                    .width(52.dp)
                    .padding(top = 3.dp),
                height = 4.dp,
            )
        }

        Text(text = "VS", style = MaterialTheme.typography.caption3, color = TextSecondary)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val bossSprite = rememberSpriteSheet(
                rememberDrawableId(
                    state.monster?.spriteRes ?: "boss_idle_spritesheet",
                    fallback = R.drawable.boss_idle_spritesheet,
                ),
                frameCount = GameConstants.SPRITE_FRAME_COUNT,
            )
            PixelSpriteAnimation(
                spriteSheet = bossSprite,
                modifier = Modifier.size(60.dp),
                mirrored = true,
            )
            StatBar(
                ratio = state.monsterHpRatio,
                color = EmberRed,
                modifier = Modifier
                    .width(60.dp)
                    .padding(top = 3.dp),
                height = 4.dp,
            )
        }
    }
}

@Composable
private fun ActionRow(
    selectedIndex: Int,
    enabled: Boolean,
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
                contentDescription = action.label,
                isSelected = index == selectedIndex,
                enabled = enabled,
                onClick = {
                    // First tap arms the action, a tap on the armed one commits it — the same
                    // two-step the crown gives, so both input paths behave identically.
                    if (index == selectedIndex) onConfirm() else onActionSelected(index)
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

private fun combatOutcomeLabel(outcome: CombatOutcome?): String = when (outcome) {
    null -> "Choisissez votre action"
    is CombatOutcome.PlayerCriticalHit -> buildString {
        append("Critique ! -${outcome.damageDealt}")
        if (outcome.lifeStolen > 0) append(" · +${outcome.lifeStolen} PV")
    }
    is CombatOutcome.PlayerStandardHit -> buildString {
        append("Touché ! -${outcome.damageDealt}")
        if (outcome.lifeStolen > 0) append(" · +${outcome.lifeStolen} PV")
    }
    is CombatOutcome.PlayerSpellCast -> "Sort ! -${outcome.damageDealt}"
    is CombatOutcome.PlayerParried -> buildString {
        append("Paré !")
        if (outcome.riposteDamage > 0) append(" Riposte -${outcome.riposteDamage}")
    }
    is CombatOutcome.PlayerDamaged -> "Aïe ! -${outcome.damageTaken} PV"
    is CombatOutcome.MonsterSlain -> "${outcome.monster.name} vaincu !"
    is CombatOutcome.PlayerDefeated -> "Vous êtes tombé…"
}
