package com.development.forge

import android.content.Intent
import android.content.res.Configuration
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.development.forge.ui.theme.ForgeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalendarGrid(
                modifier = Modifier
            )
        }
    }

    private fun compose(addresses: Array<String>, subject: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, addresses)
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}

@Composable
private fun lifecycleComposable(
    modifier: Modifier = Modifier
        .statusBarsPadding(), locationManager: LocationManager
) {

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleStateFlow = lifecycleOwner.lifecycle.currentStateFlow
    val lifecycleState = lifecycleStateFlow.collectAsState()
    val currentLifecycleState = lifecycleOwner.lifecycle.currentStateAsState()

    BackHandler(true) {

    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {

    }

    LifecycleStartEffect(locationManager, lifecycleOwner, effects = {
        val locationListener = LocationListener {

        }
        onStopOrDispose {

        }
    })

    LifecycleResumeEffect(Unit) {
        onPauseOrDispose {

        }
    }

    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
        }
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    controller: NavController = rememberNavController()
) {
    Box(
        modifier = modifier
    ) {
        Canvas(
            modifier = modifier
                .fillMaxSize()
                .background(color = Color.White)
        ) {
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

        Canvas(modifier = Modifier.size(300.dp).align(Alignment.Center)) {
            val width = size.width
            val height = size.height
            val radius = 30f
            drawCircle(color = Color.Black, radius = radius, center = Offset(0f, 0f))
            drawCircle(color = Color.Black, radius = radius, center = Offset(0f, height))
            drawCircle(color = Color.Black, radius = radius, center = Offset(width, 0f))
            drawCircle(color = Color.Black, radius = radius, center = Offset(width, height))
        }
    }
}

@Composable
private fun CalendarGrid(modifier: Modifier = Modifier) {
    HomeScreen()
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CalendarGridPreview() {
    ForgeTheme {
        CalendarGrid()
    }
}