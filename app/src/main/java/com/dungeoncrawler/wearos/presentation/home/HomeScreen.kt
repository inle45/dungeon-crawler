package com.dungeoncrawler.wearos.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.dungeoncrawler.wearos.core.theme.HealthGreen
import com.dungeoncrawler.wearos.core.theme.OledBlack
import com.dungeoncrawler.wearos.domain.model.BossEncounter
import com.dungeoncrawler.wearos.presentation.components.StatBar
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeScreen(
    onNavigateToBossCombat: (BossEncounter) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is HomeEffect.NavigateToBossCombat -> onNavigateToBossCombat(effect.boss)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = OledBlack)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val heroSprite = rememberSpriteSheet(R.drawable.hero_idle_spritesheet, frameCount = 4)
        PixelSpriteAnimation(
            spriteSheet = heroSprite,
            modifier = Modifier.size(72.dp),
        )

        Text(
            text = "Floor ${state.player.currentFloor}",
            style = MaterialTheme.typography.title3,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "${state.player.currentHp} / ${state.player.maxHp} HP",
            style = MaterialTheme.typography.caption1,
        )
        StatBar(
            ratio = state.player.hpRatio,
            color = HealthGreen,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )

        Text(
            text = "Next boss in ${2000 - state.stepsIntoBossCycle} steps",
            style = MaterialTheme.typography.caption2,
        )
        StatBar(
            ratio = state.bossProgressRatio,
            color = ArcaneBlue,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

