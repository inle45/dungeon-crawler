package com.dungeoncrawler.wearos.core.sprite

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/**
 * Renders and animates a single-row Pixel Lab AI spritesheet (1-6 frames) on a [Canvas].
 *
 * Only one [SpriteSheet.bitmap] is ever held in memory (decoded once by the caller and reused
 * across frames), and drawing pulls a sub-rectangle out of it each tick instead of allocating a
 * per-frame [androidx.compose.ui.graphics.ImageBitmap] — this keeps the animation cheap enough
 * to run continuously on a watch without pressuring the heap.
 *
 * @param frameDurationMillis time each frame stays on screen; frameCount * frameDurationMillis
 *   is the full loop length.
 * @param isPlaying pause on a held frame (e.g. frame 0) when false, useful for idle/defeated states.
 */
@Composable
fun PixelSpriteAnimation(
    spriteSheet: SpriteSheet,
    modifier: Modifier = Modifier,
    frameDurationMillis: Int = 150,
    isPlaying: Boolean = true,
    mirrored: Boolean = false,
) {
    val frameCount = spriteSheet.frameCount

    if (frameCount == 1 || !isPlaying) {
        Canvas(modifier = modifier) {
            drawSpriteFrame(spriteSheet, frameIndex = 0, mirrored = mirrored)
        }
        return
    }

    val transition = rememberInfiniteTransition(label = "pixelSpriteAnimation")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = frameCount.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = frameDurationMillis * frameCount,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pixelSpriteFrameProgress",
    )

    Canvas(modifier = modifier) {
        val frameIndex = progress.toInt().coerceIn(0, frameCount - 1)
        drawSpriteFrame(spriteSheet, frameIndex = frameIndex, mirrored = mirrored)
    }
}

private fun DrawScope.drawSpriteFrame(
    spriteSheet: SpriteSheet,
    frameIndex: Int,
    mirrored: Boolean,
) {
    val (frameWidth, frameHeight) = spriteSheet.frameSize
    val srcOffset = Offset(x = (frameIndex * frameWidth).toFloat(), y = 0f)

    val scale = minOf(size.width / frameWidth, size.height / frameHeight)
    val destWidth = frameWidth * scale
    val destHeight = frameHeight * scale
    val destOffset = Offset(
        x = (size.width - destWidth) / 2f,
        y = (size.height - destHeight) / 2f,
    )

    withTransform({
        if (mirrored) {
            scale(scaleX = -1f, scaleY = 1f, pivot = center)
        }
    }) {
        drawImage(
            image = spriteSheet.bitmap,
            srcOffset = IntOffset(srcOffset.x.toInt(), srcOffset.y.toInt()),
            srcSize = IntSize(frameWidth, frameHeight),
            dstOffset = IntOffset(destOffset.x.toInt(), destOffset.y.toInt()),
            dstSize = IntSize(destWidth.toInt(), destHeight.toInt()),
            filterQuality = FilterQuality.None, // preserve crisp pixel-art edges, no smoothing
        )
    }
}
