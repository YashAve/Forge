package com.development.forge.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.development.forge.core.components.Amber
import com.development.forge.core.designsystem.ForgeFonts
import kotlinx.coroutines.delay

@Preview
@Composable
fun GlowingDot(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .size(200.dp)
            .background(Color(0xFF14110C))
    ) {
        val quarter = Offset(size.width * 0.25f, size.height * 0.25f)
        drawCircle(
            center = quarter,
            radius = 70f,
            brush = Brush.radialGradient(
                listOf(Amber, Color.Transparent),
                center = quarter,
                radius = 50f
            )
        )
    }
}

@Preview
@Composable
fun CurvePath(modifier: Modifier = Modifier) {
    /*Now build it.
      Draw a path that starts at (50f, 200f), ends at (250f, 200f),
      and bulges upward in the middle using a control point at (150f, 50f).
      Draw it as a stroke, 4px.
      Give me the Path and the drawPath call.*/
    Canvas(
        modifier = modifier
            .size(300.dp)
            .background(color = Color.White)
    ) {
        val path = Path().apply {
            moveTo(50f, 200f)
            quadraticTo(150f, 50f, 250f, 200f)
        }
        drawPath(path, style = Stroke(width = 4f), color = Color.Black)
    }
}

@Preview
@Composable
fun CubicCurvePath(modifier: Modifier = Modifier) {
    /*Draw an S-curve, stroked 4px:
    start: (50f, 150f)
    first control: (100f, 30f) — pulls the first half up
    second control: (200f, 270f) — pulls the second half down
    end: (250f, 150f)*/
    Canvas(
        modifier = modifier
            .size(300.dp)
            .background(color = Color.White)
    ) {
        val path = Path().apply {
            moveTo(50f, 150f)
            cubicTo(100f, 30f, 200f, 270f, 250f, 150f)
        }
        drawPath(path, style = Stroke(width = 4f), color = Color.Black)
    }
}

@Preview
@Composable
fun LinearGradientRectangle(modifier: Modifier = Modifier) {
    /*Draw a filled rectangle
     — pick a simple topLeft and size
     — filled with a linear gradient that goes from red to blue.
     Give me the one drawRect call (with the Brush)*/
    Canvas(
        modifier = modifier
            .size(300.dp)
            .background(color = Color.White)
    ) {
        drawRect(
            brush = Brush.linearGradient(
                listOf(Color.Red, Color.Blue),
                start = Offset(50f, 100f),
                end = Offset(150f, 100f)
            ),
            topLeft = Offset(50f, 50f),
            size = Size(200f, 100f)
        )
    }
}

@Preview
@Composable
fun RadiantGradientCircle(modifier: Modifier = Modifier) {
    /*Draw a circle at center = (150f, 150f),
     radius = 80f, filled with a radial gradient that's orange at the center
     and transparent at the outer edge.
     One note: Compose has no Color.Orange built in
     — use a hex like Color(0xFFFF7A18).
     Give me the drawCircle call with the radial-gradient brush.*/
    Canvas(
        modifier = modifier
            .size(300.dp)
            .background(color = Color.White)
    ) {
        drawCircle(
            center = Offset(150f, 150f),
            radius = 80f,
            brush = Brush.radialGradient(
                listOf(Color(0xFFFF7A18), Color.Transparent),
                center = Offset(150f, 150f),
                radius = 80f
            )
        )
    }
}

@Preview
@Composable
fun CanvasText(modifier: Modifier = Modifier) {
    /*Draw the word "Forge" in black,
      fontSize 60.sp, at a fixed topLeft = Offset(40f, 120f)
      — no centering yet.
      Give me the full thing: the rememberTextMeasurer() line (in the right place),
      the .measure(...), and the drawText(...)*/
    val measurer = rememberTextMeasurer()
    val heatUpTextMeasurer = rememberTextMeasurer()
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
            .fillMaxSize()
            .background(color = Color(0xFF14110C))
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
        drawRect(
            topLeft = Offset(0f, 0f),
            size = Size(size.width, size.height),
            brush = Brush.radialGradient(
                listOf(
                    Color(0xFFF58A00).copy(alpha = 0.10f),
                    Color.Transparent
                ), center = Offset(size.width * 0.18f, size.height * 0.12f), radius = 420f
            )
        )
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
        val heatUpResult = heatUpTextMeasurer.measure(
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

@Preview
@Composable
fun UselessProgressBar(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF14110C))
    ) {
        drawRect(
            topLeft = Offset(0f, 0f),
            size = Size(size.width, size.height),
            brush = Brush.radialGradient(
                listOf(Color(0xFFF58A00).copy(alpha = 0.10f), Color.Transparent),
                center = Offset(size.width * 0.18f, size.height * 0.12f),
                radius = 420f
            )
        )
        val start = size.width * 0.10f
        drawRoundRect(
            topLeft = Offset(start, size.height * 0.35f),
            size = Size(size.width * 0.80f, 30f),
            cornerRadius = CornerRadius(25f, 25f),
            brush = Brush.linearGradient(
                0f to Color.Black,
                (size.width * 0.50f) to Color(0xFFF58A00),
                (size.width * 0.90f) to Color.DarkGray.copy(alpha = 0.3f),
                start = Offset(size.width * 0.10f, (size.height * 0.35f) + 30f),
                end = Offset(size.width * 0.70f, (size.height * 0.35f) + 30f)
            )
        )
        drawCircle(
            center = Offset(size.width * 0.70f, (size.height * 0.35f + 15f)),
            radius = 27f,
            color = Color.Black
        )
        drawCircle(
            center = Offset(size.width * 0.70f, (size.height * 0.35f + 15f)),
            radius = 25f,
            brush = Brush.radialGradient(
                listOf(Color(0xFFF58A00), Color.Transparent),
                Offset(size.width * 0.70f, (size.height * 0.35f + 15f)),
                radius = 150f
            )
        )
    }
}