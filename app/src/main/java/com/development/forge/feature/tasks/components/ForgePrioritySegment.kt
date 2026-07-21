package com.development.forge.feature.tasks.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.development.forge.domain.model.Priority

/**
 * ── ForgePrioritySegmented(value: Priority, onChange: (Priority) -> Unit) ── NEW
 *   ONE cohesive track with a sliding amber indicator behind the selected option.
 *   Three segments: Low, Medium, High.
 *   THE TRACK: a Row, corner 14dp, background colors.bgRaised, 1.5dp border
 *   colors.borderDefault, 4dp inner padding. Height ~50dp (segments are 42dp).
 *   THE SLIDING INDICATOR (the interesting part): an amber rounded rect (corner
 *   10dp) sitting BEHIND the segment text, that slides to the selected segment.
 *     - It occupies 1/3 of the track width, and its horizontal position animates
 *       to the selected index.
 *     - In Compose: this is exactly your BiasAlignment trick from ForgeToggle,
 *       scaled to 3 positions. Put the indicator in a Box that fills the track,
 *       aligned with BiasAlignment(horizontalBias = bias), where bias animates
 *       between -1f (Low), 0f (Medium), 1f (High) via animateFloatAsState on the
 *       EASE-SPRING curve (CubicBezier 0.34,1.56,0.64,1) — the overshoot is what
 *       makes it feel springy, matching the web's 240ms ease-spring.
 *     - The indicator width = 1/3 of the track. With BiasAlignment and a child
 *       that's fillMaxWidth(1f/3f) of the inner area, bias -1/0/1 lands it in the
 *       three thirds. Give it a glow (amber @35%, dropShadow before its clip).
 *   THE THREE SEGMENTS (on top of the indicator, zIndex above it): each a Row,
 *   weight 1f, 42dp tall, centred, gap 7dp:
 *     - a 7dp dot: colour = currentColor(fgOnAmber) when active, else colors.fgTertiary
 *     - the label (Low/Medium/High) → Space Grotesk, 14sp, W600
 *       colour animates: active → colors.fgOnAmber, inactive → colors.fgSecondary
 *       (200ms ease-out)
 *   Tapping a segment calls onChange with that Priority.
 *   NOTE: Priority enum is LOW/MED/HIGH; map to labels Low/Medium/High.*/
@Preview
@Composable
fun ForgePrioritySegmented(
    modifier: Modifier = Modifier,
    value: Priority = Priority.HIGH,
    onChange: (Priority) -> Unit = { }
) {

}