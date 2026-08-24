package com.dungeoncrawler.wearos.core.sprite

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize

/**
 * Describes a horizontal Pixel Lab AI spritesheet: a single transparent PNG laid out as
 * [frameCount] equal-width frames in one row, read left to right.
 */
data class SpriteSheet(
    val bitmap: ImageBitmap,
    val frameCount: Int,
) {
    init {
        require(frameCount in 1..MAX_FRAMES) {
            "PixelSpriteAnimation supports $MIN_FRAMES-$MAX_FRAMES frames to bound memory use, got $frameCount"
        }
    }

    val frameSize: IntSize = IntSize(bitmap.width / frameCount, bitmap.height)

    companion object {
        const val MIN_FRAMES = 1
        const val MAX_FRAMES = 6
    }
}
