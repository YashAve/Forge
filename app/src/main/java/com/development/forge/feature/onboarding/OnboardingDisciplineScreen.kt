package com.development.forge.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.components.Amber
import com.development.forge.core.designsystem.ForgeFonts

/**
 * DESIGN SPEC — Onboarding Step 3 of 3: The Discipline.
 *
 * ## Text content (same styles as the other steps)
 * - Eyebrow:  "STEP 03 · THE DISCIPLINE"  (Mono, 11sp, 0.18em, uppercase, amber)
 * - Title:    "One task\nat a time."       (Space Grotesk, 32sp, W600, 1.1em, -0.025em, #F5EDDE)
 * - Body:     "The rest of your list stays hidden until you finish what's in front of
 *              you. The next thing is the only thing."  (16sp, 1.45em, #B5A990)
 *
 * ## Bottom chrome (shared, in the flow)
 * - Pagination dots (current step = 2).
 * - Primary button label: "Forge your first task" — and on THIS step, NO arrow icon.
 *   (This is the flow's finish button — it leaves onboarding.)
 *
 * ## HERO — HeroTaskStack (three stacked cards, front one active)
 * A Box, 280dp wide × 220dp tall, centered. THREE overlapping cards, drawn back-to-front,
 * so the front (active) card sits lowest on screen and brightest. Use absolute positioning
 * (a Box with children offset via padding/offset, or align + offset).
 *
 * CARD 1 — BACK card (draw first, sits highest and dimmest):
 *   - Positioned near the top: ~32dp from top, inset ~24dp left/right.
 *   - Height 56dp, corner 16dp, background bg-raised #1C180F,
 *     1dp border border-subtle (cream 6%), opacity 0.4, scaled to 0.92.
 *
 * CARD 2 — MID card (draw second):
 *   - ~56dp from top, inset ~12dp left/right.
 *   - Height 60dp, corner 16dp, background bg-raised, 1dp border-default,
 *     opacity 0.6, scaled to 0.96.
 *
 * CARD 3 — FRONT card (draw last, the active task — full attention):
 *   - ~88dp from top, full width (no inset).
 *   - Height 88dp, corner 18dp, background bg-elev #251F14,
 *     1.5dp border amber #F58A00.
 *   - Focus ring + shadow: an amber ring (4dp, amber 0.16) + a large dark drop shadow.
 *     (Use your dropShadow / stroke-straddle ring technique.)
 *   - Padding 16dp/18dp, content is a Column with space-between:
 *     TOP ROW (icon + label + spacer + time):
 *       - An 8dp amber dot with a glow (amber 0.8 blur 10).
 *       - "NOW"  (Mono, 10sp, 0.14em, uppercase, W600, amber), 10dp gap from dot.
 *       - Spacer (weight 1) pushing the time to the right.
 *       - "25:00"  (Mono, 10sp, tertiary #7A7060).
 *     BOTTOM: the task title "Draft the proposal"
 *       (Space Grotesk, 17sp, W600, -0.01em, #F5EDDE).
 *
 * The visual story: the top two cards are the "hidden backlog" (dim, small, receding);
 * the front card is "the only task" (bright, amber-ringed, detailed). That IS the message.
 *
 * @param onNext primary button → finish onboarding.
 * @param onSkip not shown on step 3 (Skip is hidden on the last step).
 */
@Preview
@Composable
fun OnboardingDisciplineScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 28.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(text = "android")
        }
        Column(
            modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            /*## Text content (same styles as the other steps)
 * - Eyebrow:  "STEP 03 · THE DISCIPLINE"  (Mono, 11sp, 0.18em, uppercase, amber)
 * - Title:    "One task\nat a time."       (Space Grotesk, 32sp, W600, 1.1em, -0.025em, #F5EDDE)
 * - Body:     "The rest of your list stays hidden until you finish what's in front of
 *              you. The next thing is the only thing."  (16sp, 1.45em, #B5A990)*/
            Text(
                text = "STEP 03 · THE DISCIPLINE", style = TextStyle(
                    fontFamily = ForgeFonts.jetbrainsMono,
                    fontSize = 11.sp,
                    letterSpacing = (0.18).em,
                    color = Amber
                )
            )
            Text(
                text = "One task\nat a time.", style = TextStyle(
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
                text = "The rest of your list stays hidden until you finish what's in front of you. The next thing is the only thing.",
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