package com.development.forge.feature.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.components.Amber
import com.development.forge.core.components.FgTertiary
import com.development.forge.core.designsystem.ForgeFonts

/**
 * DESIGN SPEC — Onboarding Step 2 of 3: The Mechanic.
 *
 * ## Text content (same styles as the Promise screen)
 * - Eyebrow:  "STEP 02 · THE MECHANIC"   (JetBrains Mono, 11sp, 0.18em, uppercase, amber)
 * - Title:    "Heat up.\nThen cool down." (Space Grotesk, 32sp, W600, lineHeight 1.1em, -0.025em, #F5EDDE)
 * - Body:     "Work in one focused 25-minute burst. When the timer ends, you cool down
 *              for 5. That's it. That's the whole loop."
 *              (Space Grotesk, 16sp, lineHeight 1.45em, #B5A990)
 *
 * ## Bottom chrome (shared, lives in the flow — NOT in this screen)
 * - Pagination dots (current step = 1).
 * - Primary button label: "Got it" (still has the arrow_forward).
 *
 * ## HERO — HeroGauge (a horizontal timer bar, NOT rings)
 * This hero is full-width, NOT a fixed 220dp box. It is a Column with 28dp vertical gap,
 * 8dp horizontal padding, containing two parts: a gauge block and a tick row.
 *
 * PART A — the gauge block (a Box, height 72dp, using absolute-style positioning):
 *   1. TOP-LEFT label: a Row (icon + text), amber.
 *      - Icon: local_fire_department (a flame), 16sp/dp.
 *      - Text: "HEAT UP · 25 MIN"  (Mono, 11sp, 0.14em, uppercase, amber, 8dp gap from icon).
 *   2. TOP-RIGHT label: a Row (text + icon), tertiary color #7A7060.
 *      - Text: "COOL DOWN · 5 MIN"  (Mono, 11sp, 0.14em, uppercase).
 *      - Icon: ac_unit (a snowflake), 16sp/dp.
 *   3. THE BAR (pinned to the bottom of the 72dp box, 16dp up from bottom):
 *      - A pill track: height 14dp, full corner radius, background #251F14 (bg-elev),
 *        1dp border border-default (cream 10%).
 *      - A FILL inside it at 83% width, with a HORIZONTAL gradient (left→right):
 *        #6E3500 (0%) → #A85100 (20%) → #F58A00 (60%) → #FFAA2E (100%).
 *        Add an amber glow: shadow, amber at ~0.55 alpha, blur ~18.
 *        The fill is also a pill (full corner radius).
 *   4. THE EMBER DOT on the boundary (sits at 83% along the bar, on top):
 *      - 28dp circle, centered on the 83% point (offset left by half its width).
 *      - Fill: your off-center coal gradient (#FFE0A8 → #FFAA2E at 35% → #F58A00 at 75%,
 *        highlight at 40%/35% — reuse your ember code).
 *      - Glow: amber 0.8 (blur ~18) + amber 0.4 (blur ~36).
 *      - A 2dp border in bg-base #14110C (so it reads as sitting ON the bar).
 *
 * PART B — the tick row (a Row below the gauge, space-between):
 *   - Four Mono labels, 11sp, weight 500, tertiary #7A7060:
 *     "00:00"  "15:00"  "25:00"(this one AMBER)  "30:00"
 *
 * ## How to build the bar in Compose
 * - The bar track: a Box(height 14.dp, fillMaxWidth, clip pill, background bg-elev, border).
 * - The fill: a Box INSIDE the track at fillMaxWidth(0.83f), fillMaxHeight, with a
 *   Brush.horizontalGradient of the 4 colors, clipped to a pill.
 * - The ember dot: overlay it with an offset — position its center at 83% of the bar
 *   width. Easiest: put the track in a Box, and place the dot with
 *   Modifier.align + an x offset, OR draw the whole gauge in a single Canvas.
 * - The two top labels: use Alignment.TopStart and Alignment.TopEnd inside the 72dp Box.
 *
 * @param onNext primary button → go to step 3.
 * @param onSkip "Skip" → leave onboarding.
 */
@Preview
@Composable
fun OnboardingMechanicScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 28.dp)
                .padding(horizontal = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(72.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = Amber
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HEAT UP · 25 MIN", style = TextStyle(
                            fontFamily = ForgeFonts.jetbrainsMono,
                            fontSize = 11.sp,
                            letterSpacing = (0.14).em,
                            color = Amber
                        )
                    )
                }
                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COOL DOWN · 5 MIN", style = TextStyle(
                            fontFamily = ForgeFonts.jetbrainsMono,
                            fontSize = 11.sp,
                            letterSpacing = (0.14).em,
                            color = FgTertiary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.AcUnit,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = FgTertiary
                    )
                }
            }
            /*PART A — the gauge block (a Box, height 72dp, using absolute-style positioning):
 *   1. TOP-LEFT label: a Row (icon + text), amber.
 *      - Icon: local_fire_department (a flame), 16sp/dp.
 *      - Text: "HEAT UP · 25 MIN"  (Mono, 11sp, 0.14em, uppercase, amber, 8dp gap from icon).
 *   2. TOP-RIGHT label: a Row (text + icon), tertiary color #7A7060.
 *      - Text: "COOL DOWN · 5 MIN"  (Mono, 11sp, 0.14em, uppercase).
 *      - Icon: ac_unit (a snowflake), 16sp/dp.
 *   3. THE BAR (pinned to the bottom of the 72dp box, 16dp up from bottom):
 *      - A pill track: height 14dp, full corner radius, background #251F14 (bg-elev),
 *        1dp border border-default (cream 10%).
 *      - A FILL inside it at 83% width, with a HORIZONTAL gradient (left→right):
 *        #6E3500 (0%) → #A85100 (20%) → #F58A00 (60%) → #FFAA2E (100%).
 *        Add an amber glow: shadow, amber at ~0.55 alpha, blur ~18.
 *        The fill is also a pill (full corner radius).
 *   4. THE EMBER DOT on the boundary (sits at 83% along the bar, on top):
 *      - 28dp circle, centered on the 83% point (offset left by half its width).
 *      - Fill: your off-center coal gradient (#FFE0A8 → #FFAA2E at 35% → #F58A00 at 75%,
 *        highlight at 40%/35% — reuse your ember code).
 *      - Glow: amber 0.8 (blur ~18) + amber 0.4 (blur ~36).
 *      - A 2dp border in bg-base #14110C (so it reads as sitting ON the bar).
 *
 * "amber 0.8 (blur ~18)" — the first, inner glow layer: amber color at 80% opacity, spread over roughly 18px of blur/reach. This is the tighter, stronger halo close to the dot.
"amber 0.4 (blur ~36)" — the second, outer glow layer: amber at 40% opacity, spread over roughly 36px. This is the wider, fainter halo that extends further out.*/
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .height(14.dp)
                        .border(
                            1.dp,
                            Color(0xFFF5EDDE).copy(alpha = 0.10f),
                            RoundedCornerShape(percent = 50)
                        )
                        .clip(RoundedCornerShape(percent = 50))
                        .background(color = Color(0xFF251F14))
                        .background(
                            Brush.horizontalGradient(
                                0f to Color(0xFF6E3500),
                                0.20f to Color(0xFFA85100),
                                0.60f to Color(0xFFF58A00),
                                0.83f to Color(0xFFFFAA2E),
                                0.83f to Color.Transparent,
                                1f to Color.Transparent
                            )
                        )
                )

                /*"amber 0.8 (blur ~18)" — the first, inner glow layer:
             amber color at 80% opacity, spread over roughly 18px of blur/reach.
              This is the tighter, stronger halo close to the dot.
              "amber 0.4 (blur ~36)" — the second, outer glow layer: amber at 40%
               opacity, spread over roughly 36px.
                This is the wider,fainter halo that extends further out.*/
                /*Layer 1 (bottom) — outer glow: wide, faint
The widest, softest halo — it's the "ambient light"
 bleeding out furthest from the dot. Spec: amber
  at 40% opacity, spread over roughly 36dp of reach.
   This is a drawCircle with a radialGradient brush, radius ~36dp, going from amber-at-0.4
    in the center out to fully transparent at the edge. It's drawn first precisely
     because it's the biggest — everything else needs to sit inside/over it without being hidden by it.
Layer 2 — inner glow: tighter, stronger
A smaller, more intense halo sitting inside the outer one. Spec: amber at 80% opacity, reach ~18dp. Same technique — drawCircle with a radialGradient, amber-at-0.8 fading to transparent, but a smaller radius. Drawn second so it sits on top of the outer glow's fade, giving you two visually distinct "rings of light" instead of one flat blur. This two-glow-layer trick is exactly what you did for the splash ember and the hero ember — nothing new here, just reapplied.
Layer 3 — the coal itself: solid, three-dimensional
The actual 28dp-diameter dot — not flat orange, but your off-center-highlight gradient (hot near-white → bright amber → primary amber → dark edge), with the light source shifted up-and-left so it reads as a lit sphere rather than a flat circle. This is the same four-stop gradient technique from your hero ember, just at a smaller radius (14dp instead of 28dp). Drawn third, so it sits on top of both glows — the glows are the "light bleeding around it," the coal is the "actual object."*/
                Canvas(
                    modifier = Modifier
                        .matchParentSize(),
                ) {
                    val center = Offset(size.width * 0.83f, size.height / 2f)
                    val radius = 14.dp.toPx()
                    drawCircle(
                        center = center,
                        radius = 36.dp.toPx(),
                        brush = Brush.radialGradient(
                            0f to Amber.copy(alpha = 0.4f),
                            1f to Color.Transparent,
                            center = center,
                            radius = 36.dp.toPx()
                        )
                    )
                    drawCircle(
                        center = center,
                        radius = 18.dp.toPx(),
                        brush = Brush.radialGradient(
                            0f to Amber.copy(alpha = 0.8f),
                            1f to Color.Transparent,
                            center = center,
                            radius = 18.dp.toPx()
                        )
                    )
                    drawCircle(
                        center = center, radius = radius,
                        brush = Brush.radialGradient(
                            0.00f to Color(0xFFFFE0A8),
                            0.30f to Color(0xFFFFAA2E),
                            0.65f to Amber,
                            1.00f to Color(0xFFA85100),
                            center = Offset(
                                center.x - radius * 0.20f,   // 0.2 × 28dp ≈ 5.6dp to the LEFT
                                center.y - radius * 0.30f    // 0.3 × 28dp ≈ 8.4dp UP (minus, not plus)
                            ),
                            radius = radius * 1.3f
                        )
                    )
                    drawCircle(
                        radius = radius,
                        center = center,
                        color = Color(0xFF14110C),
                        style = Stroke(width = (1.5).dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("00:00", "15:00", "25:00", "30:00").forEachIndexed { index, value ->
                    Text(
                        text = value, style = TextStyle(
                            fontFamily = ForgeFonts.jetbrainsMono,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.W500,
                            color = if (index == 2) Amber else Color(0xFF7A7060)
                        )
                    )
                }
            }
        }
        Column(
            modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "STEP 02 · THE MECHANIC", style = TextStyle(
                    fontFamily = ForgeFonts.jetbrainsMono,
                    fontSize = 11.sp,
                    letterSpacing = (0.18).em,
                    color = Amber
                )
            )
            Text(
                text = "Heat up.\nThen cool down.", style = TextStyle(
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
                text = "Work in one focused 25-minute burst. When the timer ends, you cool down for 5. That's it. That's the whole loop.",
                style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = 16.sp,
                    lineHeight = (1.45).em,
                    color = Color(0xFFB5A990)
                )
            )
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}