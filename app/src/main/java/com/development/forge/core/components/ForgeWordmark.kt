package com.development.forge.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import com.development.forge.core.designsystem.ForgeTheme

@Preview
@Composable
fun ForgeWordmark(modifier: Modifier = Modifier, fontSize: TextUnit = 24.sp) {

    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val fgPrimary = ForgeTheme.colors.fgPrimary   // read here — remember{} lambda is not @Composable
    val amber = ForgeTheme.colors.amber           // captured for the DrawScope Canvas below
    val result = remember(fgPrimary, fontSize) {
        measurer.measure(
            "Forge",
            style = TextStyle(
                color = fgPrimary,
                fontFamily = ForgeFonts.spaceGrotesk,
                fontWeight = FontWeight.W700,
                fontSize = fontSize,
                letterSpacing = (-0.035).em
            )
        )
    }

    val dotOverhang = 2f
    val dotCenter = Offset(result.size.width * 0.68f, 10f)

    Canvas(
        modifier = modifier.size(
            with(density) { result.size.width.toDp() },
            with(density) { (result.size.height + dotOverhang).toDp() }
        )) {
        drawText(result, topLeft = Offset(0f, dotOverhang))
        val glowRadius = 15f
        drawCircle(
            radius = glowRadius,
            center = dotCenter,
            brush = Brush.radialGradient(
                listOf(amber.copy(alpha = 0.7f), Color.Transparent),
                center = dotCenter,
                radius = glowRadius       // matches circle radius
            )
        )
// 2. solid core SECOND (smaller, on top)
        drawCircle(color = amber, radius = 2.25f, center = dotCenter)
    }
}