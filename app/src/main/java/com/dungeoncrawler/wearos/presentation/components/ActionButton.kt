package com.dungeoncrawler.wearos.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonColors
import androidx.wear.compose.material.ButtonDefaults
import com.dungeoncrawler.wearos.core.theme.GoldAccent
import com.dungeoncrawler.wearos.core.theme.OledBlack

/** Circular icon-only action button rendered from a Pixel Lab AI icon PNG, no background chrome. */
@Composable
fun ActionButton(
    icon: Painter,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedBorderColor: Color = GoldAccent,
    colors: ButtonColors = ButtonDefaults.buttonColors(backgroundColor = OledBlack),
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, selectedBorderColor, CircleShape)
                } else {
                    Modifier
                },
            ),
        colors = colors,
    ) {
        Image(
            painter = icon,
            contentDescription = contentDescription,
            modifier = Modifier
                .padding(8.dp)
                .size(28.dp),
        )
    }
}
