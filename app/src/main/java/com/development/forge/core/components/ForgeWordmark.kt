package com.development.forge.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.designsystem.ForgeFonts

@Preview
@Composable
fun ForgeWordmark(modifier: Modifier = Modifier, fontSize: TextUnit = 24.sp) {

    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val result = remember {
        measurer.measure(
            "Forge",
            style = TextStyle(
                color = Color(0xFFF5EDDE),
                fontFamily = ForgeFonts.spaceGrotesk,
                fontWeight = FontWeight.W700,
                fontSize = fontSize,
                letterSpacing = (-0.035).em
            )
        )
    }

    Canvas(
        modifier = modifier.size(
        with(density) { result.size.width.toDp() },
        with(density) { (result.size.height + 2f + 9f).toDp() }
    )) {
        drawText(result)
        drawCircle(
            radius = 4.5f,
            center = Offset(result.size.width * 0.68f, result.size.height * 0.20f),
            color = Color.Yellow
        )
    }
}