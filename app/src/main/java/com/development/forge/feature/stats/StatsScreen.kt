package com.development.forge.feature.stats

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.components.ForgeCard
import com.development.forge.core.components.ForgeSectionLabel
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme
import com.development.forge.domain.model.Task

@Composable
fun ForgeProgressRing(
    modifier: Modifier = Modifier,
    value: Float = 0.65f,
    size: Dp = 224.dp,
    stroke: Dp = 18.dp,
    centerLabel: String = "65%",
    centerSub: String = "today"
) {
    val transition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.40f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = EaseOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    var target by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(value) { target = value }
    val animatedValue by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(900, easing = EaseOut),
        label = "sweep"
    )

    val amber = ForgeTheme.colors.amber      // captured for the DrawScope Canvas below
    val bgElev = ForgeTheme.colors.bgElev    // captured for the DrawScope Canvas below

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // ambient breathing glow
            drawCircle(
                center = center,
                radius = this.size.height / 2f,
                brush = Brush.radialGradient(
                    0.00f to amber.copy(alpha = glowAlpha),
                    0.55f to amber.copy(alpha = glowAlpha * 0.18f),
                    0.70f to Color.Transparent,
                    center = center,
                    radius = this.size.height / 2f
                )
            )

            inset(stroke.toPx() / 2f) {
                // track arc — full 360° dark groove
                drawArc(
                    color = bgElev,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = stroke.toPx(), cap = StrokeCap.Round)
                )
                // progress arc — sweeps to the percentage
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFC766),
                            Color(0xFFF58A00),
                            Color(0xFFD46E00)
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedValue,
                    useCenter = false,
                    style = Stroke(width = stroke.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // center label
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerLabel,
                style = TextStyle(
                    fontFamily = ForgeFonts.jetbrainsMono,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.W600,
                    color = ForgeTheme.colors.fgPrimary
                )
            )
            Text(
                text = centerSub.uppercase(),
                style = TextStyle(
                    fontFamily = ForgeFonts.jetbrainsMono,
                    fontSize = 12.sp,
                    letterSpacing = (0.1).em,
                    color = ForgeTheme.colors.fgTertiary
                )
            )
        }
    }
}

@Preview
@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    tasks: List<Task> = emptyList(),
    doneThisWeek: Int = 0,
    weekCompletions: List<Int> = List(7) { 0 },
    currentStreak: Int = 0,
    longestStreak: Int = 0
) {
    val total = tasks.size
    val done = tasks.filter { it.done }.size
    val percentage = if (total > 0) (done * 100 / total) else 0

    // Labels are Monday-first and [weekCompletions] is built to match, so the two
    // zip positionally. The last bar used to be `"S" to done` — the all-time done
    // count standing in for Sunday — which is why it always dwarfed the rest.
    val data = WEEKDAY_LABELS.zip(weekCompletions)
    val peak = data.maxOf { it.second }.coerceAtLeast(1)

    // Only claim a best day when something was actually completed this week.
    val bestDayIndex = weekCompletions.indices.maxByOrNull { weekCompletions[it] }
    val bestDay = bestDayIndex?.takeIf { weekCompletions[it] > 0 }?.let { WEEKDAY_NAMES[it] }

    // peak bar pulse — drives the glow alpha
    val peakTransition = rememberInfiniteTransition(label = "peak")
    val peakGlow by peakTransition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.60f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "peakGlow"
    )

    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ────────────────────────────────────────────
        Text(
            text = "Your progress",
            style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk,
                fontSize = 26.sp,
                fontWeight = FontWeight.W600,
                letterSpacing = (-0.025).em,
                color = ForgeTheme.colors.fgPrimary
            )
        )
        // Hidden at zero rather than reading "KEPT THE FIRE LIT FOR 0 DAYS", which is
        // true but says the opposite of what it claims. Same rule as the "personal
        // best" suffix and the "Best day" row: an element that has nothing to report
        // goes away instead of reporting nothing.
        if (currentStreak > 0) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "KEPT THE FIRE LIT FOR $currentStreak " +
                    if (currentStreak == 1) "DAY" else "DAYS",
                style = TextStyle(
                    fontFamily = ForgeFonts.jetbrainsMono,
                    fontSize = 12.sp,
                    letterSpacing = (0.1).em,
                    color = ForgeTheme.colors.fgTertiary
                )
            )
        }

        Spacer(Modifier.height(40.dp))

        // ── Hero ring ─────────────────────────────────────────
        ForgeProgressRing(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            value = percentage.toFloat() / 100f,
            centerLabel = "$percentage%",
            centerSub = "today"
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "$done of $total tasks done today",
            style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk,
                fontSize = 14.sp,
                color = ForgeTheme.colors.fgSecondary
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(34.dp))

        // ── Streak hero card ──────────────────────────────────
        ForgeCard(modifier = Modifier.fillMaxWidth(), accent = true) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(ForgeTheme.colors.amber.copy(alpha = 0.14f))
                        .border(
                            1.dp,
                            ForgeTheme.colors.amber.copy(alpha = 0.30f),
                            RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                        tint = ForgeTheme.colors.amber
                    )
                }
                Spacer(Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$currentStreak",
                            style = TextStyle(
                                fontFamily = ForgeFonts.jetbrainsMono,
                                fontSize = 44.sp,
                                fontWeight = FontWeight.W600,
                                lineHeight = 1.em,
                                letterSpacing = (-0.02).em,
                                color = ForgeTheme.colors.fgPrimary
                            ),
                            modifier = Modifier.alignByBaseline()
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = if (currentStreak == 1) "day" else "days",
                            style = TextStyle(
                                fontFamily = ForgeFonts.jetbrainsMono,
                                fontSize = 15.sp,
                                color = ForgeTheme.colors.fgSecondary
                            ),
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = ForgeTheme.colors.fgSecondary)) {
                                append("Current streak")
                            }
                            // Only an honest claim when the current run has actually
                            // reached the all-time longest, so it disappears rather
                            // than asserting something the data does not support.
                            if (currentStreak > 0 && currentStreak >= longestStreak) {
                                withStyle(SpanStyle(color = ForgeTheme.colors.fgTertiary)) {
                                    append(" · personal best")
                                }
                            }
                        },
                        style = TextStyle(
                            fontFamily = ForgeFonts.spaceGrotesk,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Supporting stat cards ─────────────────────────────
        Row(modifier = Modifier.fillMaxWidth()) {
            ForgeCard(modifier = Modifier.weight(1f), accent = false) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 18.dp)
                        .padding(top = 20.dp, bottom = 18.dp)
                ) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = ForgeTheme.colors.fgTertiary
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "$percentage%",
                        style = TextStyle(
                            fontFamily = ForgeFonts.jetbrainsMono,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.W600,
                            letterSpacing = (-0.02).em,
                            color = ForgeTheme.colors.fgPrimary
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Completion rate",
                        style = TextStyle(
                            fontFamily = ForgeFonts.spaceGrotesk,
                            fontSize = 12.5.sp,
                            color = ForgeTheme.colors.fgSecondary
                        )
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            ForgeCard(modifier = Modifier.weight(1f), accent = false) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 18.dp)
                        .padding(top = 20.dp, bottom = 18.dp)
                ) {
                    Icon(
                        Icons.Outlined.TaskAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = ForgeTheme.colors.fgTertiary
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "$doneThisWeek",
                        style = TextStyle(
                            fontFamily = ForgeFonts.jetbrainsMono,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.W600,
                            letterSpacing = (-0.02).em,
                            lineHeight = 1.em,
                            color = ForgeTheme.colors.fgPrimary
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Done this week",
                        style = TextStyle(
                            fontFamily = ForgeFonts.spaceGrotesk,
                            fontSize = 12.5.sp,
                            color = ForgeTheme.colors.fgSecondary
                        ),
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(Modifier.height(34.dp))

        // ── Week chart ────────────────────────────────────────
        ForgeSectionLabel(label = "This week")
        Spacer(Modifier.height(12.dp))
        ForgeCard(modifier = Modifier.fillMaxWidth(), accent = false) {
            Column(
                modifier = Modifier.padding(
                    start = 20.dp, end = 20.dp,
                    top = 22.dp, bottom = 16.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    data.forEach { (day, value) ->
                        val isPeak = value == peak && value > 0
                        val barHeightTarget = (value.toFloat() / peak * 92f)
                            .coerceAtLeast(6f).dp

                        // entry animation per bar
                        var targetHeight by remember { mutableStateOf(0.dp) }
                        LaunchedEffect(barHeightTarget) { targetHeight = barHeightTarget }
                        val animatedHeight by animateDpAsState(
                            targetValue = targetHeight,
                            animationSpec = tween(700, easing = EaseOut),
                            label = "bar"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.78f)
                                        .height(animatedHeight)
                                        .then(
                                            // glow BEFORE clip — same rule as every
                                            // other glowing element in this project
                                            if (isPeak) Modifier.dropShadow(
                                                shape = RoundedCornerShape(6.dp),
                                                shadow = Shadow(
                                                    color = ForgeTheme.colors.amber.copy(alpha = peakGlow),
                                                    offset = DpOffset(0.dp, 0.dp),
                                                    radius = 12.dp
                                                )
                                            ) else Modifier
                                        )
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isPeak) Brush.verticalGradient(
                                                listOf(Color(0xFFFFC766), Color(0xFFF58A00))
                                            ) else Brush.verticalGradient(
                                                listOf(
                                                    ForgeTheme.colors.amber.copy(alpha = 0.28f),
                                                    ForgeTheme.colors.amber.copy(alpha = 0.28f)
                                                )
                                            )
                                        )
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = day.uppercase(),
                                    style = TextStyle(
                                        fontFamily = ForgeFonts.jetbrainsMono,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.06.em,
                                        color = if (isPeak) ForgeTheme.colors.amberA300
                                        else ForgeTheme.colors.fgTertiary
                                    )
                                )
                            }
                        }
                    }
                }

                // footer — best day. The whole row goes away when nothing has been
                // completed this week: a "best day" with zero completions would be a
                // fabricated answer, and the divider above it would be decorating
                // nothing.
                if (bestDay != null) {
                    Spacer(Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(ForgeTheme.colors.borderSubtle)
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Best day",
                            style = TextStyle(
                                fontFamily = ForgeFonts.spaceGrotesk,
                                fontSize = 13.sp,
                                color = ForgeTheme.colors.fgSecondary
                            )
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = ForgeTheme.colors.amberA300
                            )
                            Text(
                                text = bestDay,
                                style = TextStyle(
                                    fontFamily = ForgeFonts.spaceGrotesk,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.W600,
                                    color = ForgeTheme.colors.amberA300
                                )
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/** Chart labels, Monday first. Index matches the `weekCompletions` parameter. */
private val WEEKDAY_LABELS = listOf("M", "T", "W", "T", "F", "S", "S")

/** Full names for the same seven slots, used by the "Best day" footer. */
private val WEEKDAY_NAMES = listOf(
    "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
)
