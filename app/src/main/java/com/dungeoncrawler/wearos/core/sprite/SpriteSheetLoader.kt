package com.dungeoncrawler.wearos.core.sprite

import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext

/**
 * Decodes a Pixel Lab AI spritesheet drawable exactly once per [resId] and [remember]s the
 * resulting [ImageBitmap] across recompositions, so [PixelSpriteAnimation] never re-decodes a
 * PNG on every frame tick.
 */
@Composable
fun rememberSpriteSheet(@DrawableRes resId: Int, frameCount: Int): SpriteSheet {
    val context = LocalContext.current
    return remember(resId, frameCount) {
        val bitmap = BitmapFactory.decodeResource(context.resources, resId)
        SpriteSheet(bitmap = bitmap.asImageBitmap(), frameCount = frameCount)
    }
}
