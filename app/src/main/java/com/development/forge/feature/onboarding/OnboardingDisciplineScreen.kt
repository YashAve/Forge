package com.development.forge.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

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
    
}