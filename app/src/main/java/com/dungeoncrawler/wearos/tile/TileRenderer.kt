package com.dungeoncrawler.wearos.tile

import androidx.wear.tiles.ColorBuilders.argb
import androidx.wear.tiles.DimensionBuilders.dp
import androidx.wear.tiles.DimensionBuilders.expand
import androidx.wear.tiles.DimensionBuilders.weight
import androidx.wear.tiles.LayoutElementBuilders.Box
import androidx.wear.tiles.LayoutElementBuilders.Column
import androidx.wear.tiles.LayoutElementBuilders.FontStyle
import androidx.wear.tiles.LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER
import androidx.wear.tiles.LayoutElementBuilders.LayoutElement
import androidx.wear.tiles.LayoutElementBuilders.Row
import androidx.wear.tiles.LayoutElementBuilders.Spacer
import androidx.wear.tiles.LayoutElementBuilders.Text
import androidx.wear.tiles.ModifiersBuilders.Background
import androidx.wear.tiles.ModifiersBuilders.Corner
import androidx.wear.tiles.ModifiersBuilders.Modifiers
import androidx.wear.tiles.ModifiersBuilders.Padding

private const val OLED_BLACK = 0xFF000000.toInt()
private const val EMBER_RED = 0xFFC62828.toInt()
private const val TEXT_PRIMARY = 0xFFF2F2F2.toInt()
private const val TEXT_FAINT = 0xFF8F8F8F.toInt()
private const val TRACK_GRAY = 0x33FFFFFF

/**
 * The tile's layout tree, drawn on a pure black canvas: dungeon name, floor X/10, HP, and the
 * bar toward the next boss.
 */
object TileRenderer {

    fun render(
        dungeonName: String,
        currentFloor: Int,
        totalFloors: Int,
        currentHp: Int,
        maxHp: Int,
        stepsIntoBossCycle: Long,
        stepsPerBossEncounter: Int,
        accentColorArgb: Int,
    ): LayoutElement {
        val bossRatio = (stepsIntoBossCycle.toFloat() / stepsPerBossEncounter).coerceIn(0.02f, 1f)
        val stepsRemaining = stepsPerBossEncounter - stepsIntoBossCycle

        return Box.Builder()
            .setWidth(expand())
            .setHeight(expand())
            .setModifiers(
                Modifiers.Builder()
                    .setBackground(Background.Builder().setColor(argb(OLED_BLACK)).build())
                    .setPadding(Padding.Builder().setAll(dp(14f)).build())
                    .build(),
            )
            .addContent(
                Column.Builder()
                    .setWidth(expand())
                    .setHeight(expand())
                    .setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
                    .addContent(text(dungeonName, accentColorArgb, sizeSp = 12f))
                    .addContent(spacer(4f))
                    .addContent(text("Étage $currentFloor/$totalFloors", TEXT_PRIMARY, sizeSp = 17f))
                    .addContent(spacer(6f))
                    .addContent(text("$currentHp / $maxHp PV", EMBER_RED, sizeSp = 13f))
                    .addContent(spacer(8f))
                    .addContent(progressBar(bossRatio, accentColorArgb))
                    .addContent(spacer(4f))
                    .addContent(text("Boss dans $stepsRemaining pas", TEXT_FAINT, sizeSp = 10f))
                    .build(),
            )
            .build()
    }

    private fun text(value: String, colorArgb: Int, sizeSp: Float): LayoutElement =
        Text.Builder()
            .setText(value)
            .setMaxLines(1)
            .setFontStyle(FontStyle.Builder().setColor(argb(colorArgb)).setSize(dp(sizeSp)).build())
            .build()

    private fun spacer(heightDp: Float): LayoutElement =
        Spacer.Builder().setHeight(dp(heightDp)).build()

    private fun progressBar(ratio: Float, fillColorArgb: Int): LayoutElement =
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
                                            .setColor(argb(fillColorArgb))
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
