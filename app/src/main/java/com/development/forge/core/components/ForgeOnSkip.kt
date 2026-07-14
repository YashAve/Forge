package com.development.forge.core.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.designsystem.ForgeFonts

val OnForgeOnSkip: @Composable (onClick: () -> Unit) -> Unit = { onClick ->
    val interactionSource = remember { MutableInteractionSource() }
    Text(
        text = "Skip",
        fontSize = 14.sp,
        fontFamily = ForgeFonts.spaceGrotesk,
        fontWeight = FontWeight.W600,
        color = Color(0xFF7A7060),
        letterSpacing = (-0.005).em,
        modifier = Modifier.clickable(
            enabled = true,
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    )
}

@Preview
@Composable
private fun OnForgeOnSkipPreview() {
    OnForgeOnSkip {

    }
}