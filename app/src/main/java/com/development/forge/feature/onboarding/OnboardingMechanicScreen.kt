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

@Preview
@Composable
fun OnboardingMechanicScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 28.dp)
    ) {
        // ── Hero area ─────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Center   // centre the whole gauge
        ) {
            Spacer(Modifier.height(28.dp))

            // labels row — flame left, snowflake right
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)   // just enough for the label row
            ) {
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = Amber
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "HEAT UP · 25 MIN",
                        style = TextStyle(
                            fontFamily = ForgeFonts.jetbrainsMono,
                            fontSize = 11.sp,
                            letterSpacing = (0.14).em,
                            color = Amber
                        )
                    )
                }
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COOL DOWN · 5 MIN",
                        style = TextStyle(
                            fontFamily = ForgeFonts.jetbrainsMono,
                            fontSize = 11.sp,
                            letterSpacing = (0.14).em,
                            color = FgTertiary
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Default.AcUnit,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = FgTertiary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))   // gap between labels and bar

            // bar + ember dot
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                // the pill track + gradient fill
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
                        .background(Color(0xFF251F14))
                        .background(
                            Brush.horizontalGradient(
                                0f    to Color(0xFF6E3500),
                                0.20f to Color(0xFFA85100),
                                0.60f to Color(0xFFF58A00),
                                0.83f to Color(0xFFFFAA2E),
                                0.83f to Color.Transparent,
                                1f    to Color.Transparent
                            )
                        )
                )

                // ember dot — glows + coal, centered on the 83% mark
                Canvas(modifier = Modifier.matchParentSize()) {
                    val center = Offset(size.width * 0.83f, size.height / 2f)
                    val radius = 14.dp.toPx()

                    // outer glow — wider, fainter
                    drawCircle(
                        center = center,
                        radius = 36.dp.toPx(),
                        brush = Brush.radialGradient(
                            colors = listOf(Amber.copy(alpha = 0.4f), Color.Transparent),
                            center = center,
                            radius = 36.dp.toPx()
                        )
                    )
                    // inner glow — tighter, stronger
                    drawCircle(
                        center = center,
                        radius = 18.dp.toPx(),
                        brush = Brush.radialGradient(
                            colors = listOf(Amber.copy(alpha = 0.8f), Color.Transparent),
                            center = center,
                            radius = 18.dp.toPx()
                        )
                    )
                    // coal — off-center 3D highlight
                    drawCircle(
                        center = center,
                        radius = radius,
                        brush = Brush.radialGradient(
                            0.00f to Color(0xFFFFE0A8),
                            0.30f to Color(0xFFFFAA2E),
                            0.65f to Amber,
                            1.00f to Color(0xFFA85100),
                            center = Offset(
                                center.x - radius * 0.20f,
                                center.y - radius * 0.30f
                            ),
                            radius = radius * 1.3f
                        )
                    )
                    // separation border
                    drawCircle(
                        radius = radius,
                        center = center,
                        color = Color(0xFF14110C),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }

            Spacer(Modifier.height(16.dp))   // gap between bar and tick row

            // tick row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("00:00", "15:00", "25:00", "30:00").forEachIndexed { index, value ->
                    Text(
                        text = value,
                        style = TextStyle(
                            fontFamily = ForgeFonts.jetbrainsMono,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.W500,
                            color = if (index == 2) Amber else Color(0xFF7A7060)
                        )
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
        }

        // ── Text content ──────────────────────────────────────
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "STEP 02 · THE MECHANIC",
                style = TextStyle(
                    fontFamily = ForgeFonts.jetbrainsMono,
                    fontSize = 11.sp,
                    letterSpacing = (0.18).em,
                    color = Amber
                )
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Heat up.\nThen cool down.",
                style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.W600,
                    lineHeight = (1.1).em,
                    letterSpacing = (-0.025).em,
                    color = Color(0xFFF5EDDE)
                )
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Work in one focused 25-minute burst. When the timer ends, you cool down for 5. That's it. That's the whole loop.",
                style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = 16.sp,
                    lineHeight = (1.45).em,
                    color = Color(0xFFB5A990)
                )
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}