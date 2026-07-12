package com.development.forge.feature.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.development.forge.core.components.Amber

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
    val density = LocalDensity.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 28.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .heightIn(min = 240.dp)
                .weight(1f)
        ) {
            /*## HeroEmber — a pure Canvas piece (the fun part)
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
 * Ask me before building the hero and I'll teach it properly.*/
            val radiusCenter = 180f
            val radiusMiddle = 130f
            val radiusOuter = 90f
            Canvas(
                modifier = Modifier
                    .size(220.dp)
                    .align(Alignment.Center)
            ) {
                drawCircle(
                    radius = radiusCenter,
                    center = Offset(size.width / 2f, size.height / 2f),
                    brush = Brush.radialGradient(
                        0f to Amber,
                        0.70f to Amber.copy(alpha = 0.16f),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = radiusCenter
                    )
                )
                drawCircle(
                    radius = radiusMiddle,
                    center = Offset(size.width / 2f, size.height / 2f),
                    brush = Brush.radialGradient(
                        0f to Amber,
                        0.70f to Amber.copy(alpha = 0.16f),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = radiusMiddle
                    )
                )
                drawCircle(
                    radius = radiusOuter,
                    center = Offset(size.width / 2f, size.height / 2f),
                    brush = Brush.radialGradient(
                        0f to Amber,
                        0.70f to Amber.copy(alpha = 0.16f),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = radiusOuter
                    )
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

        }
    }
}