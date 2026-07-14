package com.development.forge.feature.onboarding

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
 *      - A 2dp border in bg-base #14110C (so it reads as sitting ON the bar).*/
            Box(
                modifier = Modifier
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
            Spacer(modifier = Modifier.height(16.dp))
            /** PART B — the tick row (a Row below the gauge, space-between):
             *   - Four Mono labels, 11sp, weight 500, tertiary #7A7060:
             *     "00:00"  "15:00"  "25:00"(this one AMBER)  "30:00"*/
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
            /*Text content (same styles as the Promise screen)
 * - Eyebrow:  "STEP 02 · THE MECHANIC"   (JetBrains Mono, 11sp, 0.18em, uppercase, amber)
 * - Title:    "Heat up.\nThen cool down." (Space Grotesk, 32sp, W600, lineHeight 1.1em, -0.025em, #F5EDDE)
 * - Body:     "Work in one focused 25-minute burst. When the timer ends, you cool down
 *              for 5. That's it. That's the whole loop."
 *              (Space Grotesk, 16sp, lineHeight 1.45em, #B5A990)*/
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