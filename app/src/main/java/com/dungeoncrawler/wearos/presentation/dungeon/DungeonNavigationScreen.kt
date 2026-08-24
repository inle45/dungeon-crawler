package com.dungeoncrawler.wearos.presentation.dungeon

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.dungeoncrawler.wearos.R
import com.dungeoncrawler.wearos.core.sprite.PixelSpriteAnimation
import com.dungeoncrawler.wearos.core.sprite.rememberSpriteSheet
import com.dungeoncrawler.wearos.core.theme.ArcaneBlue
import com.dungeoncrawler.wearos.core.theme.GoldAccent
import com.dungeoncrawler.wearos.core.theme.HealthGreen
import com.dungeoncrawler.wearos.core.theme.OledBlack
import com.dungeoncrawler.wearos.core.theme.TextSecondary
import com.dungeoncrawler.wearos.domain.GameConstants
import com.dungeoncrawler.wearos.domain.model.Dungeon
import com.dungeoncrawler.wearos.domain.model.MicroEvent
import com.dungeoncrawler.wearos.presentation.components.StatBar
import kotlinx.coroutines.flow.collectLatest

/**
 * The idle screen: which dungeon and floor the hero is on, how close the next boss is, and the
 * last thing that happened while the player was walking.
 */
@Composable
fun DungeonNavigationScreen(
    onNavigateToBossCombat: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToDungeonClear: () -> Unit,
    viewModel: DungeonNavigationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                DungeonNavigationEffect.NavigateToBossCombat -> onNavigateToBossCombat()
                DungeonNavigationEffect.NavigateToInventory -> onNavigateToInventory()
                DungeonNavigationEffect.NavigateToDungeonClear -> onNavigateToDungeonClear()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = state.dungeon.name,
            style = MaterialTheme.typography.caption1,
            color = Color(state.dungeon.accentColorArgb),
            textAlign = TextAlign.Center,
            maxLines = 2,
        )

        FloorPips(
            currentFloor = state.progress.currentFloor,
            accent = Color(state.dungeon.accentColorArgb),
            modifier = Modifier.padding(top = 6.dp),
        )

        Text(
            text = state.floorLabel,
            style = MaterialTheme.typography.title3,
            modifier = Modifier.padding(top = 2.dp),
        )

        val heroSprite = rememberSpriteSheet(
            R.drawable.hero_idle_spritesheet,
            frameCount = GameConstants.SPRITE_FRAME_COUNT,
        )
        PixelSpriteAnimation(
            spriteSheet = heroSprite,
            modifier = Modifier
                .size(56.dp)
                .clickable { viewModel.processIntent(DungeonNavigationIntent.OpenInventory) },
        )

        state.power?.let { power ->
            Text(
                text = "${power.currentHp} / ${power.maxHp} PV",
                style = MaterialTheme.typography.caption2,
            )
            StatBar(
                ratio = power.hpRatio,
                color = HealthGreen,
                modifier = Modifier.padding(top = 3.dp, bottom = 8.dp),
            )
        }

        Text(
            text = if (state.isFinalFloor) {
                "Boss suprême dans ${state.stepsToNextBoss} pas"
            } else {
                "Boss d'étage dans ${state.stepsToNextBoss} pas"
            },
            style = MaterialTheme.typography.caption3,
            color = TextSecondary,
        )
        StatBar(
            ratio = state.bossProgressRatio,
            color = if (state.isFinalFloor) GoldAccent else ArcaneBlue,
            modifier = Modifier.padding(top = 3.dp),
        )

        state.lastMicroEvent?.let { event ->
            Text(
                text = event.label(),
                style = MaterialTheme.typography.caption3,
                color = GoldAccent,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { viewModel.processIntent(DungeonNavigationIntent.DismissMicroEvent) },
            )
        }
    }
}

/** Ten pips, one per floor — the dungeon's shape read at a glance without a number. */
@Composable
private fun FloorPips(currentFloor: Int, accent: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
    ) {
        repeat(Dungeon.FLOORS_PER_DUNGEON) { index ->
            val floor = index + 1
            FloorPip(
                color = when {
                    floor < currentFloor -> accent
                    floor == currentFloor -> GoldAccent
                    else -> TextSecondary.copy(alpha = 0.25f)
                },
            )
        }
    }
}

@Composable
private fun FloorPip(color: Color) {
    Box(
        modifier = Modifier
            .size(width = 8.dp, height = 3.dp)
            .background(color, RoundedCornerShape(2.dp)),
    )
}

private fun MicroEvent.label(): String = when (this) {
    is MicroEvent.Loot -> "Butin : ${item.name}"
    is MicroEvent.Trap -> "Piège ! -$damage PV"
    is MicroEvent.MicroMobSlain -> buildString {
        append(monster.name)
        append(" vaincu")
        loot?.let { append(" · ${it.name}") }
    }
}
