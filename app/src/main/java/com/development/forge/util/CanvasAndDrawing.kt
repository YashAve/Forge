package com.development.forge.util

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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