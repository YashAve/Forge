package com.development.forge.feature.onboarding

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.components.Amber
import com.development.forge.core.designsystem.ForgeFonts

@Preview
@Composable
fun OnboardingPromiseScreen(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "ember")

    val scale1 by transition.ringScale(0)
    val alpha1 by transition.ringAlpha(0)
    val scale2 by transition.ringScale(700)
    val alpha2 by transition.ringAlpha(700)
    val scale3 by transition.ringScale(1400)
    val alpha3 by transition.ringAlpha(1400)

    Column(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 28.dp)
    ) {
        // ── Hero area ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()           // ← was fillMaxSize, removed
                .heightIn(min = 240.dp)
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(220.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radiusAmber = 28.dp.toPx()

                // three staggered pulsing rings
                drawRing(center, 90.dp.toPx(), scale1, alpha1)
                drawRing(center, 65.dp.toPx(), scale2, alpha2)
                drawRing(center, 45.dp.toPx(), scale3, alpha3)

                // outer glow — wider, fainter
                drawCircle(
                    center = center,
                    radius = 80.dp.toPx(),
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Amber.copy(alpha = 0.50f),   // bumped from 0.35
                            Color.Transparent
                        ),
                        center = center,
                        radius = 80.dp.toPx()
                    )
                )

                // inner glow — tighter, stronger
                drawCircle(
                    center = center,
                    radius = 40.dp.toPx(),
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Amber.copy(alpha = 0.85f),   // bumped from 0.70
                            Color.Transparent
                        ),
                        center = center,
                        radius = 40.dp.toPx()
                    )
                )

                // the ember coal — off-center 3D highlight
                drawCircle(
                    center = center,
                    radius = radiusAmber,
                    brush = Brush.radialGradient(
                        0.00f to Color(0xFFFFE0A8),
                        0.30f to Color(0xFFFFAA2E),
                        0.65f to Amber,
                        1.00f to Color(0xFFA85100),
                        center = Offset(
                            center.x - radiusAmber * 0.20f,
                            center.y - radiusAmber * 0.30f
                        ),
                        radius = radiusAmber * 1.3f
                    )
                )
            }
        }

        // ── Text content ──────────────────────────────────────
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "STEP 01 · THE PROMISE",
                style = TextStyle(
                    fontFamily = ForgeFonts.jetbrainsMono,
                    fontSize = 11.sp,
                    letterSpacing = (0.18).em,
                    color = Amber
                )
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Stop putting\nit off.",
                style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.W600,
                    lineHeight = (1.1).em,
                    letterSpacing = (-0.025).em,
                    color = Color(0xFFF5EDDE)
                )
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Forge turns the next task into the only task. No backlog, no badges screaming at you, no guilt.",
                style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = 16.sp,
                    lineHeight = (1.45).em,
                    color = Color(0xFFB5A990)
                )
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}

private fun DrawScope.drawRing(
    center: Offset,
    baseRadius: Float,
    scale: Float,
    alpha: Float
) {
    val r = baseRadius * scale
    drawCircle(
        radius = r,
        center = center,
        brush = Brush.radialGradient(
            colors = listOf(
                Amber.copy(alpha = 0.28f * alpha),   // bumped from 0.16f
                Color.Transparent
            ),
            center = center,
            radius = r
        )
    )
}

@Composable
private fun InfiniteTransition.ringScale(delayMs: Int) = animateFloat(
    initialValue = 0.55f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
        animation = tween(2800, easing = EaseOut),
        repeatMode = RepeatMode.Restart,
        initialStartOffset = StartOffset(delayMs)
    ),
    label = "scale$delayMs"
)

@Composable
private fun InfiniteTransition.ringAlpha(delayMs: Int) = animateFloat(
    initialValue = 0.95f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
        animation = tween(2800, easing = EaseOut),
        repeatMode = RepeatMode.Restart,
        initialStartOffset = StartOffset(delayMs)
    ),
    label = "alpha$delayMs"
)