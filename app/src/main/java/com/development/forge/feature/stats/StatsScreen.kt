package com.development.forge.feature.stats

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.components.Amber
import com.development.forge.core.components.ForgeCard
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.domain.model.Task

/**
 * DESIGN SPECIFICATION — Statistics / Progress screen
 * Source: Forge Design System, AppScreens.jsx → StatsView
 *
 * The progress screen — a bottom-nav destination (the "Stats" tab). Shows a hero
 * completion ring, a wide streak stat, two supporting stat cards, and a week
 * chart. This is the app's signature data-viz screen and contains its ONE new
 * Canvas piece: the arc-based ProgressRing.
 *
 * ═══════════════════════════════════════════════════════════════
 * CHROME
 * ═══════════════════════════════════════════════════════════════
 * No top bar (title is in-screen). Bottom nav IS shown (Stats tab active). The
 * whole screen SCROLLS — a plain Column with verticalScroll (bounded content,
 * not a long list, so no LazyColumn). Padding: 24dp sides, 24dp top, 32dp bottom.
 *
 * ═══════════════════════════════════════════════════════════════
 * DATA — derived from the task list
 * ═══════════════════════════════════════════════════════════════
 *   val total = tasks.size (or a "today" subset — the prototype uses all)
 *   val done  = tasks.count { it.done }
 *   val pct   = if (total > 0) done * 100 / total else 0
 * The streak (9), "done this week" (27), and week-chart data are hardcoded in the
 * prototype — for the real app these come from history you don't track yet.
 * Hardcode them as sample values for now.
 *
 * ═══════════════════════════════════════════════════════════════
 * 1. HEADER
 * ═══════════════════════════════════════════════════════════════
 * Title "Your progress" → Space Grotesk, 26sp, W600, letterSpacing -0.025em, #F5EDDE.
 * 6dp gap, then a subtitle → JetBrains Mono, 12sp, letterSpacing 0.1em, UPPERCASE,
 * #7A7060. e.g. "KEPT THE FIRE LIT FOR 9 DAYS".
 *
 * ═══════════════════════════════════════════════════════════════
 * 2. HERO RING (the new Canvas piece — see ProgressRing spec below)
 * ═══════════════════════════════════════════════════════════════
 * Centered, 40dp top margin / 12dp bottom margin. Size 224dp, stroke 18dp.
 * centerLabel = "$pct%", centerSub = "today".
 * Below it, centered: "$done of $total tasks done today"
 *   → Space Grotesk, 14sp, #B5A990.
 *
 * ═══════════════════════════════════════════════════════════════
 * 3. STAT HERO — the wide streak card (34dp top margin)
 * ═══════════════════════════════════════════════════════════════
 * A ForgeCard(accent = true), padding 22dp vertical / 24dp horizontal, a Row with
 * a 20dp gap, vertically centered:
 *   LEFT — an icon tile: 64dp square, 18dp corner, background amber @14% alpha,
 *          1dp border amber @30% alpha. Inside: local_fire_department (FILLED),
 *          34dp, amber (#F58A00).
 *   RIGHT — a Column (weight 1f):
 *     - a Row, baseline-aligned, 7dp gap:
 *         the value "9" → JetBrains Mono, 44sp, W600, lineHeight 1, letterSpacing
 *         -0.02em, #F5EDDE
 *         the unit "days" → JetBrains Mono, 15sp, #B5A990
 *     - 8dp gap, then the label line → Space Grotesk, 14sp, #B5A990:
 *         "Current streak" + " · personal best" (the sub part in #7A7060 tertiary)
 *
 * ═══════════════════════════════════════════════════════════════
 * 4. SUPPORTING STAT CARDS (14dp top margin)
 * ═══════════════════════════════════════════════════════════════
 * A Row, 14dp gap, two equal cards (each weight 1f):
 *   StatCard #1: icon check_circle, value "$pct%", label "Completion rate"
 *   StatCard #2: icon task_alt, value "27", label "Done this week"
 *
 * Each StatCard is a ForgeCard(accent = false), padding 20dp top / 18dp sides /
 * 18dp bottom, a Column:
 *   - an icon at top: 18dp, #7A7060 (tertiary) — or amber if tint=true (not used here)
 *   - 14dp gap
 *   - the value → JetBrains Mono, 28sp, W600, letterSpacing -0.02em, lineHeight 1, #F5EDDE
 *   - 6dp gap
 *   - the label → Space Grotesk, 12.5sp, #B5A990
 *
 * ═══════════════════════════════════════════════════════════════
 * 5. WEEK CHART (34dp top margin)
 * ═══════════════════════════════════════════════════════════════
 * A ForgeSectionLabel "This week", 12dp gap, then a ForgeCard(accent = false),
 * padding 22dp top / 20dp sides / 16dp bottom, containing:
 *
 *   a) WeekBars — a Row of 7 bars, aligned to the bottom (Alignment.Bottom), 8dp
 *      gap, fixed height ~120dp. Each bar:
 *        - a column: the bar on top, its day label below (6dp gap)
 *        - bar width 78% of its cell, height = (value / peak) * 92dp, min 6dp,
 *          corner 6dp
 *        - the PEAK bar (highest value): a vertical gradient #FFC766 → #F58A00,
 *          with a glow (amber @40%, ~12dp) AND a subtle infinite pulse
 *          (rememberInfiniteTransition — the "alive" ambient motion)
 *        - non-peak bars: flat amber @28% alpha, no glow
 *        - bars animate their HEIGHT on entry (animateFloatAsState / animateDpAsState,
 *          700ms ease-out — grow up from the bottom)
 *        - day label → JetBrains Mono, 10sp, letterSpacing 0.06em, UPPERCASE;
 *          amber-300 (#FFAA2E) for the peak, #7A7060 otherwise
 *      Sample data: 7 days of values, e.g. listOf("M" to 3, "T" to 5, "W" to 2,
 *      "T" to 6, "F" to 4, "S" to 1, "S" to done).
 *
 *   b) A footer row: 14dp top margin, a 1dp top border (border-subtle, cream 6%),
 *      14dp top padding, SpaceBetween:
 *        - "Best day" → Space Grotesk, 13sp, #B5A990
 *        - a Row (6dp gap): a trophy icon (16dp, FILLED) + "Thursday", both
 *          amber-300 (#FFAA2E), weight 600
 *
 * ═══════════════════════════════════════════════════════════════
 * ═══════════════════════════════════════════════════════════════
 * THE NEW COMPONENT — ProgressRing (arc-based Canvas)
 * ═══════════════════════════════════════════════════════════════
 * ═══════════════════════════════════════════════════════════════
 * ForgeProgressRing(value: Float, size: Dp = 224.dp, stroke: Dp = 18.dp,
 *                   centerLabel: String, centerSub: String)
 *
 * This is your FIRST arc drawing. Instead of drawCircle, you use drawArc — which
 * draws a portion of a circle's outline, defined by a start angle and a sweep
 * angle. A full circle is 360°; a 65% ring is a 234° sweep.
 *
 * STRUCTURE — a Box(size × size), containing:
 *
 * a) AMBIENT GLOW (behind everything) — a soft radial ember glow that breathes.
 *    A Canvas (or a Box with a radial-gradient background) slightly larger than
 *    the ring: amber @34% at center → amber @6% at 55% → transparent at 70%.
 *    Pulse it with rememberInfiniteTransition (scale or alpha, ~3.6s, ease-out) so
 *    it's alive at rest. This is the ambient motion that makes Stats feel dynamic.
 *
 * b) THE RING — a Canvas(matchParentSize or size). Inside, TWO arcs:
 *
 *    The arc geometry: both arcs are drawn in the same square region, inset by
 *    half the stroke so the stroke doesn't clip at the edges. Compute:
 *      val strokePx = stroke.toPx()
 *      val inset = strokePx / 2
 *      val arcSize = Size(size.toPx() - strokePx, size.toPx() - strokePx)
 *      val topLeft = Offset(inset, inset)
 *    Both drawArc calls use these same topLeft + arcSize.
 *
 *    TRACK ARC (drawn first — the empty groove):
 *      drawArc(
 *        color = Color(0xFF251F14),       // bg-elev
 *        startAngle = -90f,               // start at 12 o'clock (top)
 *        sweepAngle = 360f,               // full circle
 *        useCenter = false,               // stroke only, not a filled pie
 *        style = Stroke(width = strokePx, cap = StrokeCap.Round),
 *        topLeft = topLeft, size = arcSize
 *      )
 *
 *    PROGRESS ARC (drawn on top — the filled portion):
 *      drawArc(
 *        brush = Brush.linearGradient(     // ember gradient sweep
 *          listOf(Color(0xFFFFC766), Color(0xFFF58A00), Color(0xFFD46E00))
 *        ),
 *        startAngle = -90f,                // ALSO start at the top
 *        sweepAngle = 360f * animatedValue, // sweep proportional to progress
 *        useCenter = false,
 *        style = Stroke(width = strokePx, cap = StrokeCap.Round),
 *        topLeft = topLeft, size = arcSize
 *      )
 *
 *    KEY CONCEPTS:
 *    - startAngle = -90f: Canvas angles start at 3 o'clock (0°) and go CLOCKWISE.
 *      -90° rotates the start to 12 o'clock (top), which is where a progress ring
 *      should begin. This replaces the CSS "rotate(-90deg)" trick.
 *    - sweepAngle = 360 * value: value 0.65 → 234° of arc. The track is the full
 *      360°; the progress arc is only as long as the percentage.
 *    - StrokeCap.Round: rounds the ends of the arc (matches strokeLinecap="round").
 *    - useCenter = false: draw only the curved stroke, NOT a filled wedge/pie.
 *
 * c) THE ENTRY ANIMATION — the arc sweeps from 0 to its value on appearance.
 *    val animatedValue by animateFloatAsState(
 *      targetValue = value,
 *      animationSpec = tween(900, easing = EaseOut),
 *      label = "ringSweep"
 *    )
 *    But animateFloatAsState only animates when the target CHANGES — on first
 *    composition it's already at `value`, no animation. To get the sweep-from-0
 *    on entry, start at 0 and set the real value in a LaunchedEffect:
 *      var target by remember { mutableFloatStateOf(0f) }
 *      LaunchedEffect(value) { target = value }
 *      val animatedValue by animateFloatAsState(target, tween(900, EaseOut), ...)
 *    Now on entry: target flips 0 → value, and the arc sweeps up over 900ms.
 *
 * d) THE CENTER LABEL (a Column centered in the Box, over the ring):
 *    - centerLabel "$pct%" → JetBrains Mono, ~40sp, W600, #F5EDDE
 *    - centerSub "today"   → JetBrains Mono, ~12sp, letterSpacing 0.1em,
 *      UPPERCASE, #7A7060
 *
 * ═══════════════════════════════════════════════════════════════
 * PARAMETERS (screen)
 * ═══════════════════════════════════════════════════════════════
 * @param tasks     the task list (for total/done/pct).
 * @param modifier  applied to the root Column.
 */
@Preview
@Composable
fun ForgeProgressRing(
    modifier: Modifier = Modifier,
    value: Float = 0.65f,        // 0.0–1.0 fraction, NOT a percentage integer
    size: Dp = 224.dp,
    stroke: Dp = 18.dp,
    centerLabel: String = "65%",
    centerSub: String = "today"
) {
    val transition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.20f, targetValue = 0.40f, animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = EaseOut), repeatMode = RepeatMode.Reverse
        ), label = "glowAlpha"
    )

    // entry animation — starts at 0, LaunchedEffect sets real value
    var target by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(value) { target = value }
    val animatedValue by animateFloatAsState(
        targetValue = target, animationSpec = tween(900, easing = EaseOut), label = "sweep"
    )

    Box(
        modifier = modifier.size(size), contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // ambient breathing glow — behind the ring
            drawCircle(
                center = center, radius = this.size.height / 2f, brush = Brush.radialGradient(
                    0.00f to Amber.copy(alpha = glowAlpha),
                    0.55f to Amber.copy(alpha = glowAlpha * 0.18f),
                    0.70f to Color.Transparent,
                    center = center,
                    radius = this.size.height / 2f
                )
            )

            // inset shifts the coordinate system inward by strokePx/2 on all sides
            // so drawArc uses the already-shrunk size — do NOT pass size = arcSize
            inset(stroke.toPx() / 2f) {

                // track arc — full 360° dark groove
                drawArc(
                    color = Color(0xFF251F14),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = stroke.toPx(), cap = StrokeCap.Round)
                )

                // progress arc — sweeps proportional to value (0.0–1.0)
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFC766), Color(0xFFF58A00), Color(0xFFD46E00)
                        )
                    ), startAngle = -90f, sweepAngle = 360f * animatedValue,   // 0.65 → 234°
                    useCenter = false, style = Stroke(width = stroke.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // center label — sits over the ring
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerLabel, style = TextStyle(
                    fontFamily = ForgeFonts.jetbrainsMono,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.W600,
                    color = Color(0xFFF5EDDE)
                )
            )
            Text(
                text = centerSub.uppercase(), style = TextStyle(
                    fontFamily = ForgeFonts.jetbrainsMono,
                    fontSize = 12.sp,
                    letterSpacing = (0.1).em,
                    color = Color(0xFF7A7060)
                )
            )
        }
    }
}

@Preview
@Composable
fun StatsScreen(
    modifier: Modifier = Modifier, tasks: List<Task> = com.development.forge.domain.model.tasks
) {
    val total = tasks.size
    val done = tasks.filter { it.done }.size
    val percentage = if (total > 0) (done * 100 / total) else 0

    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Your progress", style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk,
                fontSize = 26.sp,
                fontWeight = FontWeight.W600,
                letterSpacing = (-0.025).em,
                color = Color(0xFFF5EDDE)
            )
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "KEPT THE FIRE LIT FOR 9 DAYS", style = TextStyle(
                fontFamily = ForgeFonts.jetbrainsMono,
                fontSize = 12.sp,
                letterSpacing = (0.1).em,
                color = Color(0xFF7A7060)
            )
        )
        Spacer(Modifier.height(40.dp))

        // hero ring — value is fraction (0.0–1.0), label shows the integer %
        ForgeProgressRing(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            value = percentage.toFloat() / 100f,
            centerLabel = "$percentage%",
            centerSub = "today"
        )

        Spacer(Modifier.height(12.dp))
        Text(
            text = "$done of $total tasks done today", style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk, fontSize = 14.sp, color = Color(0xFFB5A990)
            ), modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(34.dp))

        /** ═══════════════════════════════════════════════════════════════
         * 3. STAT HERO — the wide streak card (34dp top margin)
         * ═══════════════════════════════════════════════════════════════
         * A ForgeCard(accent = true), padding 22dp vertical / 24dp horizontal, a Row with
         * a 20dp gap, vertically centered:
         *   LEFT — an icon tile: 64dp square, 18dp corner, background amber @14% alpha,
         *          1dp border amber @30% alpha. Inside: local_fire_department (FILLED),
         *          34dp, amber (#F58A00).
         *   RIGHT — a Column (weight 1f):
         *     - a Row, baseline-aligned, 7dp gap:
         *         the value "9" → JetBrains Mono, 44sp, W600, lineHeight 1, letterSpacing
         *         -0.02em, #F5EDDE
         *         the unit "days" → JetBrains Mono, 15sp, #B5A990
         *     - 8dp gap, then the label line → Space Grotesk, 14sp, #B5A990:
         *         "Current streak" + " · personal best" (the sub part in #7A7060 tertiary)*/
        ForgeCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Amber.copy(0.14f))
                        .border(
                            width = 1.dp,
                            color = Amber.copy(alpha = 0.30f),
                            shape = RoundedCornerShape(18.dp)
                        ), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.LocalFireDepartment,
                        null,
                        modifier = Modifier.size(34.dp),
                        tint = Amber
                    )
                }
                Column(modifier = Modifier.weight(1f)) {

                }
            }
        }
    }
}