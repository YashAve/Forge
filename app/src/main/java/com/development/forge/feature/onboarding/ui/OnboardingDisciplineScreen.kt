package com.development.forge.feature.onboarding.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme

/**
 * DESIGN SPEC — Onboarding Step 3 of 3: The Discipline.
 *
 * Text: eyebrow "STEP 03 · THE DISCIPLINE", title "One task\nat a time.",
 * body about the list staying hidden.
 *
 * HERO — HeroTaskStack: 280x220 box, three overlapping cards.
 *  - Cards 1 & 2 (dim, receding backlog) → drawn in Canvas (flat rects, no shadow).
 *  - Card 3 (the active task) → a real composable Box, because it needs a blurred
 *    drop shadow + a focus ring + real text content. Canvas has no blur.
 *
 * Bottom chrome (dots + "Forge your first task" button, NO arrow) lives in the flow.
 */
@Preview
@Composable
fun OnboardingDisciplineScreen(modifier: Modifier = Modifier) {
    val amber = ForgeTheme.colors.amber            // captured for DrawScope (Canvas / drawBehind)
    val bgRaised = ForgeTheme.colors.bgRaised      // captured for DrawScope
    val fgTertiary = ForgeTheme.colors.fgTertiary  // captured for DrawScope
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 28.dp)
    ) {
        // ---------- HERO ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(220.dp)
            ) {
                // Cards 1 & 2 — flat, dim, receding. Canvas is fine here.
                Canvas(modifier = Modifier.matchParentSize()) {
                    val pivot = Offset(size.width / 2f, 0f)

                    // CARD 1 — back card: 32dp top, 24dp inset, 56dp tall, 0.4 alpha, 0.92 scale
                    scale(0.92f, 0.92f, pivot) {
                        val inset = 24.dp.toPx()
                        val topLeft = Offset(inset, 32.dp.toPx())
                        val cardSize = Size(size.width - (2 * inset), 56.dp.toPx())
                        val corner = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                        drawRoundRect(
                            topLeft = topLeft,
                            size = cardSize,
                            color = bgRaised.copy(alpha = 0.4f),
                            cornerRadius = corner
                        )
                        drawRoundRect(
                            topLeft = topLeft,
                            size = cardSize,
                            color = fgTertiary.copy(alpha = 0.06f),
                            cornerRadius = corner,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // CARD 2 — mid card: 56dp top, 12dp inset, 60dp tall, 0.6 alpha, 0.96 scale
                    scale(0.96f, 0.96f, pivot) {
                        val inset = 12.dp.toPx()
                        val topLeft = Offset(inset, 56.dp.toPx())
                        val cardSize = Size(size.width - (2 * inset), 60.dp.toPx())
                        val corner = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                        drawRoundRect(
                            topLeft = topLeft,
                            size = cardSize,
                            color = bgRaised.copy(alpha = 0.6f),
                            cornerRadius = corner
                        )
                        drawRoundRect(
                            topLeft = topLeft,
                            size = cardSize,
                            color = fgTertiary.copy(alpha = 0.10f),
                            cornerRadius = corner,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }

                // CARD 3 — the active task. A real composable: shadow + ring + content.
                Box(
                    modifier = Modifier
                        .padding(top = 88.dp)
                        .fillMaxWidth()
                        .height(88.dp)
                        .dropShadow(
                            shape = RoundedCornerShape(18.dp),
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = DpOffset(0.dp, 20.dp),
                                radius = 44.dp
                            )
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .background(ForgeTheme.colors.bgElev)
                        .border(1.5.dp, amber, RoundedCornerShape(18.dp))
                        .drawBehind {
                            // 4dp amber focus ring — straddles the edge, half sits outside
                            drawRoundRect(
                                color = amber.copy(alpha = 0.16f),
                                cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
                                style = Stroke(width = 4.dp.toPx())
                            )
                        }
                        .padding(vertical = 16.dp, horizontal = 18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 8dp amber dot + glow. Canvas sized 24dp so the glow isn't clipped.
                            Canvas(modifier = Modifier.size(24.dp)) {
                                val c = Offset(size.width / 2f, size.height / 2f)
                                drawCircle(
                                    center = c,
                                    radius = 10.dp.toPx(),
                                    brush = Brush.radialGradient(
                                        0f to amber.copy(alpha = 0.8f),
                                        1f to Color.Transparent,
                                        center = c,
                                        radius = 10.dp.toPx()
                                    )
                                )
                                drawCircle(center = c, radius = 4.dp.toPx(), color = amber)
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "NOW",
                                style = TextStyle(
                                    fontFamily = ForgeFonts.jetbrainsMono,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.14.em,
                                    fontWeight = FontWeight.W600,
                                    color = amber
                                )
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "25:00",
                                style = TextStyle(
                                    fontFamily = ForgeFonts.jetbrainsMono,
                                    fontSize = 10.sp,
                                    color = fgTertiary
                                )
                            )
                        }
                        Text(
                            text = "Draft the proposal",
                            style = TextStyle(
                                fontFamily = ForgeFonts.spaceGrotesk,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.W600,
                                letterSpacing = (-0.01).em,
                                color = ForgeTheme.colors.fgPrimary
                            )
                        )
                    }
                }
            }
        }

        // ---------- TEXT ----------
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "STEP 03 · THE DISCIPLINE",
                style = TextStyle(
                    fontFamily = ForgeFonts.jetbrainsMono,
                    fontSize = 11.sp,
                    letterSpacing = 0.18.em,
                    color = amber
                )
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "One task\nat a time.",
                style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.W600,
                    lineHeight = 1.1.em,
                    letterSpacing = (-0.025).em,
                    color = ForgeTheme.colors.fgPrimary
                )
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "The rest of your list stays hidden until you finish what's in front of you. The next thing is the only thing.",
                style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = 16.sp,
                    lineHeight = 1.45.em,
                    color = ForgeTheme.colors.fgSecondary
                )
            )
        }
    }
}