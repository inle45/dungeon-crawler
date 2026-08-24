package com.dungeoncrawler.wearos.tile

import androidx.wear.tiles.ColorBuilders.argb
import androidx.wear.tiles.DimensionBuilders.dp
import androidx.wear.tiles.DimensionBuilders.expand
import androidx.wear.tiles.DimensionBuilders.weight
import androidx.wear.tiles.LayoutElementBuilders.Box
import androidx.wear.tiles.LayoutElementBuilders.Column
import androidx.wear.tiles.LayoutElementBuilders.FontStyle
import androidx.wear.tiles.LayoutElementBuilders.LayoutElement
import androidx.wear.tiles.LayoutElementBuilders.Row
import androidx.wear.tiles.LayoutElementBuilders.Text
import androidx.wear.tiles.ModifiersBuilders.Background
import androidx.wear.tiles.ModifiersBuilders.Corner
import androidx.wear.tiles.ModifiersBuilders.Modifiers
import androidx.wear.tiles.ModifiersBuilders.Padding

private const val OLED_BLACK = 0xFF000000.toInt()
private const val EMBER_RED = 0xFFC62828.toInt()
private const val ARCANE_BLUE = 0xFF3F8CFF.toInt()
private const val TRACK_GRAY = 0x33FFFFFF

/** Builds the tile's layout tree: HP readout + a boss-progress bar on a pure black canvas. */
object TileRenderer {

    fun render(
        currentHp: Int,
        maxHp: Int,
        stepsIntoBossCycle: Long,
        stepsPerBossEncounter: Int,
    ): LayoutElement {
        val bossRatio = (stepsIntoBossCycle.toFloat() / stepsPerBossEncounter).coerceIn(0.02f, 1f)

        return Box.Builder()
            .setWidth(expand())
            .setHeight(expand())
            .setModifiers(
                Modifiers.Builder()
                    .setBackground(Background.Builder().setColor(argb(OLED_BLACK)).build())
                    .setPadding(Padding.Builder().setAll(dp(12f)).build())
                    .build(),
            )
            .addContent(
                Column.Builder()
                    .setWidth(expand())
                    .setHeight(expand())
                    .addContent(
                        Text.Builder()
                            .setText("$currentHp / $maxHp HP")
                            .setFontStyle(FontStyle.Builder().setColor(argb(EMBER_RED)).setSize(dp(16f)).build())
                            .build(),
                    )
                    .addContent(progressBar(bossRatio))
                    .addContent(
                        Text.Builder()
                            .setText("Next boss")
                            .setFontStyle(FontStyle.Builder().setColor(argb(TRACK_GRAY)).setSize(dp(11f)).build())
                            .build(),
                    )
                    .build(),
            )
            .build()
    }

    private fun progressBar(ratio: Float): LayoutElement =
        Box.Builder()
            .setWidth(expand())
            .setHeight(dp(6f))
            .setModifiers(
                Modifiers.Builder()
                    .setBackground(
                        Background.Builder()
                            .setColor(argb(TRACK_GRAY))
                            .setCorner(Corner.Builder().setRadius(dp(3f)).build())
                            .build(),
                    )
                    .setPadding(Padding.Builder().setTop(dp(6f)).setBottom(dp(6f)).build())
                    .build(),
            )
            .addContent(
                Row.Builder()
                    .setWidth(expand())
                    .setHeight(expand())
                    .addContent(
                        Box.Builder()
                            .setWidth(weight(ratio))
                            .setHeight(expand())
                            .setModifiers(
                                Modifiers.Builder()
                                    .setBackground(
                                        Background.Builder()
                                            .setColor(argb(ARCANE_BLUE))
                                            .setCorner(Corner.Builder().setRadius(dp(3f)).build())
                                            .build(),
                                    )
                                    .build(),
                            )
                            .build(),
                    )
                    .addContent(Box.Builder().setWidth(weight(1f - ratio)).setHeight(expand()).build())
                    .build(),
            )
            .build()
}
