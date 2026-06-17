package com.development.forge

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.development.forge.ui.theme.ForgeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalendarGrid()
        }
    }
}

@Composable
private fun CalendarGrid(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize().background(color = Color.White)) {
        val (boxWidth, boxHeight) = arrayOf(80f, 80f)
        val gap = 5f
        var (coveredWidth, coveredHeight) = arrayOf(50f, 50f)
        (0..4).forEach { _ ->
            var (columnStartOffset, columnTopOffset) = arrayOf(coveredWidth, coveredHeight)
            (0..6).forEach { _ ->
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(columnStartOffset, columnTopOffset),
                    size = Size(boxWidth, boxHeight)
                )
                columnStartOffset += (boxWidth + gap)
            }
            coveredHeight += boxHeight + gap
            coveredWidth = 50f
        }
    }
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CalendarGridPreview() {
    ForgeTheme {
        CalendarGrid()
    }
}