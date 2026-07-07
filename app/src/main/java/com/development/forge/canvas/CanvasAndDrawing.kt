package com.development.forge.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun BlueFilledRectangle(modifier: Modifier = Modifier) {
    /*Draw one filled blue rectangle that:
            starts 50px from the top and 50px from the left, and
            is 100px wide and 60px tall.*/
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        drawRect(color = Color.Blue, topLeft = Offset(50f, 50f), size = Size(100f, 60f))
    }
}

@Preview
@Composable
private fun StrokedRectangle(modifier: Modifier = Modifier) {
    /*Take the exact same rectangle you just wrote — same topLeft, same size, same blue — but draw it as an outline only, with a stroke 5px thick.*/
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        drawRect(
            color = Color.Blue,
            style = Stroke(width = 5f),
            topLeft = Offset(50f, 50f),
            size = Size(100f, 60f)
        )
    }
}

@Preview
@Composable
private fun StartToBottomLine(modifier: Modifier = Modifier) {
    /*Draw one line that goes from the top-left corner of the canvas to the bottom-right corner, 3px thick.*/
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        drawLine(
            color = Color.Black,
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
            strokeWidth = 3f
        )
    }
}

@Preview
@Composable
private fun TransparentRedRectangle(modifier: Modifier = Modifier) {
    /*Draw a filled red rectangle at 50% opacity — position and size are up to you, keep them simple. Just show me one drawRect call that makes red half-transparent.*/
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        drawRect(
            color = Color.Red.copy(alpha = 0.5f),
            topLeft = Offset(50f, 50f),
            size = Size(100f, 60f)
        )
    }
}

@Preview
@Composable
private fun Triangle(modifier: Modifier = Modifier) {
    /*Draw a filled triangle using a Path, with these three vertices:
        top: (150f, 50f)
        bottom-left: (50f, 250f)
        bottom-right: (250f, 250f)*/
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        val path = Path().apply {
            moveTo(150f, 50f)
            lineTo(50f, 250f)
            lineTo(250f, 250f)
            close()
        }
        drawPath(path = path, color = Color.Black)
    }
}

@Preview
@Composable
private fun QuadraticCurve(modifier: Modifier = Modifier) {
    /*Draw a path that starts at (50f, 200f),
     ends at (250f, 200f), and bulges upward in the middle
     — using a control point at (150f, 50f). Draw it as a stroke (say 4px).*/
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        val path = Path().apply {
            moveTo(200f, 0f)
            quadraticTo(400f, 400f, 200f, 800f)
        }
        drawPath(path = path, style = Stroke(width = 4f), color = Color.Black)
    }
}