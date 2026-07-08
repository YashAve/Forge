package com.development.forge.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.development.forge.core.designsystem.ForgeFonts
import kotlinx.coroutines.delay

/** Placeholder splash screen — just centers the Forge wordmark for now. */

@Preview
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {

    val measurer = rememberTextMeasurer()
    val emberScale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        delay(600)
        emberScale.apply {
            animateTo(1.18f, animationSpec = tween(800))
            animateTo(1f, tween(800))
        }
    }

    Canvas(
        modifier = modifier
    ) {
        val result = measurer.measure(
            text = "Forge",
            style = TextStyle(
                fontSize = 72.sp,
                color = Color(0xFFF5EDDE),
                fontFamily = ForgeFonts.spaceGrotesk,
                fontWeight = FontWeight.W700
            )
        )
        val textTopLeft = Offset(
            (size.width - result.size.width) / 2f,
            (size.height - result.size.height) / 2f
        )
        val dotCenter = Offset(textTopLeft.x + result.size.width * 0.68f, textTopLeft.y - 20f)
        drawText(
            result,
            topLeft = textTopLeft
        )
        val glowRadius = 40f * emberScale.value
        drawCircle(
            radius = glowRadius,
            center = dotCenter,
            brush = Brush.radialGradient(
                listOf(Color(0xFFF58A00), Color.Transparent),
                center = dotCenter,
                radius = glowRadius
            )
        )
        drawCircle(color = Color(0xFFF58A00), radius = 10f * emberScale.value, center = dotCenter)
        val heatUpResult = measurer.measure(
            "HEAT UP",
            style = TextStyle(
                color = Color(0xFFF5EDDE).copy(alpha = 0.60f),
                fontFamily = ForgeFonts.spaceGrotesk,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        )
        drawText(
            heatUpResult,
            topLeft = Offset((size.width - heatUpResult.size.width) / 2f, size.height * 0.80f)
        )
    }
}
