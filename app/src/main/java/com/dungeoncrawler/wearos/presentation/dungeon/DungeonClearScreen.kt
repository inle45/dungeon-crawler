package com.dungeoncrawler.wearos.presentation.dungeon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.dungeoncrawler.wearos.R
import com.dungeoncrawler.wearos.core.sprite.PixelSpriteAnimation
import com.dungeoncrawler.wearos.core.sprite.rememberSpriteSheet
import com.dungeoncrawler.wearos.core.theme.GoldAccent
import com.dungeoncrawler.wearos.core.theme.OledBlack
import com.dungeoncrawler.wearos.core.theme.TextSecondary
import com.dungeoncrawler.wearos.domain.model.DungeonClearChoice
import com.dungeoncrawler.wearos.presentation.components.RotarySelector
import com.dungeoncrawler.wearos.presentation.components.rememberDrawableId
import kotlinx.coroutines.flow.collectLatest

/**
 * Shown once floor 10's supreme boss falls. Two ways forward — farm this dungeon again for
 * better gear, or step into the one it just unlocked — pickable by touch or by the crown.
 */
@Composable
fun DungeonClearScreen(
    onChoiceApplied: () -> Unit,
    viewModel: DungeonClearViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is DungeonClearEffect.ChoiceApplied -> onChoiceApplied()
            }
        }
    }

    RotarySelector(
        onScrollStep = { steps -> viewModel.processIntent(DungeonClearIntent.RotateSelection(steps)) },
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OledBlack)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "DONJON TERMINÉ",
                style = MaterialTheme.typography.caption3,
                color = GoldAccent,
            )

            Text(
                text = state.dungeon.name,
                style = MaterialTheme.typography.title3,
                color = Color(state.dungeon.accentColorArgb),
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.padding(top = 2.dp),
            )

            val bossSprite = rememberSpriteSheet(
                resId = rememberDrawableId(
                    state.supremeBoss?.spriteRes ?: "boss_idle_spritesheet",
                    fallback = R.drawable.boss_idle_spritesheet,
                ),
                frameCount = state.supremeBoss?.spriteFrameCount ?: 1,
            )
            PixelSpriteAnimation(
                spriteSheet = bossSprite,
                modifier = Modifier.size(52.dp),
                // The boss is down: hold frame 0 rather than looping any idle animation.
                isPlaying = false,
            )

            state.supremeBoss?.let { boss ->
                Text(
                    text = "${boss.name} vaincu",
                    style = MaterialTheme.typography.caption3,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }

            state.loot?.let { loot ->
                Text(
                    text = "Butin : ${loot.name}",
                    style = MaterialTheme.typography.caption3,
                    color = Color(loot.rarity.colorArgb),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            state.choices.forEachIndexed { index, choice ->
                ChoiceChip(
                    choice = choice,
                    nextDungeonName = state.nextDungeon?.name,
                    isSelected = index == state.selectedIndex,
                    enabled = !state.isApplying,
                    onClick = {
                        if (index == state.selectedIndex) {
                            viewModel.processIntent(DungeonClearIntent.Confirm(choice))
                        } else {
                            viewModel.processIntent(DungeonClearIntent.SelectChoice(index))
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ChoiceChip(
    choice: DungeonClearChoice,
    nextDungeonName: String?,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Chip(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .then(
                if (isSelected) Modifier.border(1.dp, GoldAccent, RoundedCornerShape(26.dp)) else Modifier,
            ),
        colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF0D0D0D)),
        label = {
            Text(
                text = choice.label,
                style = MaterialTheme.typography.button,
                color = if (isSelected) GoldAccent else MaterialTheme.colors.onSurface,
                maxLines = 1,
            )
        },
        secondaryLabel = {
            Text(
                text = when (choice) {
                    DungeonClearChoice.NEXT_DUNGEON -> nextDungeonName ?: choice.description
                    DungeonClearChoice.FARM_AGAIN -> choice.description
                },
                style = MaterialTheme.typography.caption3,
                color = TextSecondary,
                maxLines = 2,
            )
        },
    )
}
