package com.dungeoncrawler.wearos.core.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

private val DungeonCrawlerColors = Colors(
    primary = EmberRed,
    primaryVariant = GoldAccent,
    secondary = ArcaneBlue,
    secondaryVariant = ArcaneBlue,
    error = WarningAmber,
    // background stays absolute black on every surface: no gradients, no scrims,
    // so OLED pixels stay off and battery drain is minimized.
    background = OledBlack,
    surface = OledBlack,
    onPrimary = OledBlack,
    onSecondary = OledBlack,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    onError = OledBlack,
)

@Composable
fun DungeonCrawlerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = DungeonCrawlerColors,
        content = content,
    )
}
