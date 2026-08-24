package com.dungeoncrawler.wearos.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.dungeoncrawler.wearos.R

/**
 * Resolves the drawable name carried by a domain model (an item's `iconRes`, a monster's
 * `spriteRes`) to a real resource id, so the domain layer never has to know about `R`.
 */
@Composable
@DrawableRes
fun rememberDrawableId(name: String, @DrawableRes fallback: Int = R.drawable.icon_attack_sword): Int {
    val context = LocalContext.current
    return remember(name) {
        val id = context.resources.getIdentifier(name, "drawable", context.packageName)
        if (id != 0) id else fallback
    }
}
