package com.dungeoncrawler.wearos.presentation.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import kotlin.math.abs
import kotlin.math.sign

/**
 * Wraps [content] so the rotating crown steps through a short list of choices — combat actions,
 * or the two ways out of a cleared dungeon. Each accumulated scroll past [stepThreshold] pixels
 * advances the selection by one and calls [onScrollStep] with +1 or -1.
 */
@Composable
fun RotarySelector(
    onScrollStep: (Int) -> Unit,
    modifier: Modifier = Modifier,
    stepThreshold: Float = 24f,
    content: @Composable () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    var accumulated by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .onRotaryScrollEvent { event ->
                accumulated += event.verticalScrollPixels
                if (abs(accumulated) >= stepThreshold) {
                    onScrollStep(accumulated.sign.toInt())
                    accumulated = 0f
                }
                true
            },
    ) {
        content()
    }
}
