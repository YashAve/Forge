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
import androidx.compose.foundation.layout.fillMaxSize
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

/**
 * DESIGN SPEC — Onboarding Step 1 of 3 (Onboarding.jsx → OnboardingStep1).
 *
 * ## Top bar — special case
 * - Wordmark on the LEFT (24.dp padding) + a "Skip" text button on the RIGHT
 *   (top 24, right 24): **14.sp**, W600, color #7A7060, letterSpacing (-0.005).em.
 * - Skip shows on steps 1 and 2, NOT on step 3.
 * - Easiest path: add an optional `trailing` composable slot to ForgeTopBar
 *   and pass the Skip button there.
 *
 * ## Layout (horizontal padding 24.dp; body reserves ~180.dp at the bottom
 *    for the dots + button chrome)
 * 1. HERO area — takes the upper half: weight(1f), min height **240.dp**,
 *    centered. Contains the HeroEmber (spec below).
 * 2. Eyebrow: "STEP 01 · THE PROMISE"
 *    → **MONO**, **11.sp**, letterSpacing **0.18.em**, UPPERCASE, amber.
 * 3. Spacer **12.dp**
 * 4. Title: "Stop putting\nit off."
 *    → Space Grotesk, **32.sp**, W600, lineHeight **1.1.em**, (-0.025).em, #F5EDDE
 * 5. Spacer **12.dp**
 * 6. Body: "Forge turns the next task into the only task. No backlog,
 *    no badges screaming at you, no guilt."
 *    → **16.sp**, lineHeight **1.45.em**, #B5A990
 *
 * ## Bottom chrome (fixed at bottom 28.dp, gap 24.dp between rows)
 * 7. Pagination dots, centered, gap **8.dp**, 3 dots:
 *    - Every dot: height **8.dp**, pill shape (full corner radius).
 *    - ACTIVE dot: width **24.dp**, amber.
 *    - INACTIVE dot: width **8.dp**, color cream at 18% alpha.
 *    - Width and color ANIMATE when the active dot changes:
 *      **200ms**, ease-out. (animateDpAsState + animateColorAsState.)
 * 8. Primary button "Show me how" with arrow_forward.
 *    (On step 3 the label is "Forge your first task" and has NO arrow.)
 *
 * ## HeroEmber — a pure Canvas piece (the fun part)
 * A 220.dp box, centered. Two layers:
 * - THREE pulsing rings, diameters **180 / 130 / 90 dp**, all centered:
 *   - Each ring = a radial-gradient circle: amber at **16% alpha** in the
 *     center → fully transparent at **70%** of the radius (color stops!).
 *   - Animation "ring-pulse": scale **0.55 → 1.05** while alpha
 *     **0.95 → 0** (reaches 0 at 80% of the cycle, stays 0).
 *   - Duration **2800ms**, ease-out, repeats FOREVER.
 *   - Staggered starts: ring delays **0 / 700 / 1400 ms** — this is what
 *     makes it look like heat radiating outward.
 * - The EMBER in the middle: a **56.dp** circle.
 *   - Fill: a radial gradient with an OFF-CENTER highlight (center at
 *     40% x / 35% y of the circle): **#FFE0A8 → #FFAA2E at 30% → #F58A00
 *     at 65% → #A85100** at the edge. Off-center light = looks 3D, like
 *     a glowing coal lit from one side.
 *   - Glow behind it: two radial layers, amber 70% (radius ~40) and
 *     amber 35% (radius ~80) — your splash glow pattern, doubled.
 *
 * NOTE: the forever-repeating rings need `rememberInfiniteTransition` —
 * a new animation tool you haven't used yet (Animatable was one-shot).
 * Ask me before building the hero and I'll teach it properly.
 *
 * @param onNext tap the primary button → go to step 2.
 * @param onSkip tap "Skip" → leave onboarding.
 */
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .heightIn(min = 240.dp)
                .weight(1f)
        ) {
            Canvas(
                modifier = Modifier
                    .size(220.dp)
                    .align(Alignment.Center)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radiusAmber = 28.dp.toPx()

                drawRing(center, 90.dp.toPx(), scale1, alpha1)
                drawRing(center, 65.dp.toPx(), scale2, alpha2)
                drawRing(center, 45.dp.toPx(), scale3, alpha3)

                drawCircle(
                    center = center, radius = 80.dp.toPx(),
                    brush = Brush.radialGradient(
                        0f to Amber.copy(alpha = 0.35f),
                        1f to Color.Transparent,
                        center = center,
                        radius = 80.dp.toPx()
                    )
                )
                drawCircle(
                    center = center, radius = 40.dp.toPx(),
                    brush = Brush.radialGradient(
                        0f to Amber.copy(alpha = 0.70f),
                        1f to Color.Transparent,
                        center = center,
                        radius = 40.dp.toPx()
                    )
                )
                drawCircle(
                    center = center, radius = radiusAmber,
                    brush = Brush.radialGradient(
                        0.00f to Color(0xFFFFE0A8),
                        0.30f to Color(0xFFFFAA2E),
                        0.65f to Amber,
                        1.00f to Color(0xFFA85100),
                        center = Offset(
                            center.x - radiusAmber * 0.20f,   // 0.2 × 28dp ≈ 5.6dp to the LEFT
                            center.y - radiusAmber * 0.30f    // 0.3 × 28dp ≈ 8.4dp UP (minus, not plus)
                        ),
                        radius = radiusAmber * 1.3f
                    )
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "STEP 01 · THE PROMISE", style = TextStyle(
                    fontFamily = ForgeFonts.jetbrainsMono,
                    fontSize = 11.sp,
                    letterSpacing = (0.18).em,
                    color = Amber
                )
            )
            Text(
                text = "Stop putting\nit off.", style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.W600,
                    lineHeight = (1.1).em,
                    letterSpacing = (-0.025).em,
                    color = Color(0xFFF5EDDE)
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Forge turns the next task into the only task. No backlog, no badges screaming at you, no guilt.",
                style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = 16.sp,
                    lineHeight = (1.45).em,
                    color = Color(0xFFB5A990)
                )
            )
            Spacer(modifier = Modifier.height(28.dp))
            /*## Bottom chrome (fixed at bottom 28.dp, gap 24.dp between rows)
 * 7. Pagination dots, centered, gap **8.dp**, 3 dots:
 *    - Every dot: height **8.dp**, pill shape (full corner radius).
 *    - ACTIVE dot: width **24.dp**, amber.
 *    - INACTIVE dot: width **8.dp**, color cream at 18% alpha.
 *    - Width and color ANIMATE when the active dot changes:
 *      **200ms**, ease-out. (animateDpAsState + animateColorAsState.)
 * 8. Primary button "Show me how" with arrow_forward.
 *    (On step 3 the label is "Forge your first task" and has NO arrow.)*/
        }
    }
}

private fun DrawScope.drawRing(center: Offset, baseRadius: Float, scale: Float, alpha: Float) {
    val r = baseRadius * scale
    drawCircle(
        radius = r,
        center = center,
        brush = Brush.radialGradient(
            0.0f to Amber.copy(alpha = 0.16f * alpha),
            0.7f to Color.Transparent,
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