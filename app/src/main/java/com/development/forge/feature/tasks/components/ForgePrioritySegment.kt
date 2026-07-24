package com.development.forge.feature.tasks.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme
import com.development.forge.domain.model.Priority

/**
 * ═══════════════════════════════════════════════════════════════
 * 5. PRIORITY GROUP (28dp top margin) — segmented control (NEW component)
 * ═══════════════════════════════════════════════════════════════
 * A ForgeSectionLabel "Priority", 10dp gap, then ForgePrioritySegmented.
 *
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
    /**   THE TRACK: a Row, corner 14dp, background colors.bgRaised, 1.5dp border
     *   colors.borderDefault, 4dp inner padding. Height ~50dp (segments are 42dp).
     *   THE SLIDING INDICATOR (the interesting part): an amber rounded rect (corner
     *   10dp) sitting BEHIND the segment text, that slides to the selected segment.*/
    val targetBias = when (value) {
        Priority.LOW -> -1f
        Priority.MED -> 0f
        Priority.HIGH -> 1f
    }

    var selected by rememberSaveable { mutableStateOf(Priority.MED) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(ForgeTheme.colors.bgRaised)
            .border(
                border = BorderStroke(
                    width = (1.5).dp,
                    color = ForgeTheme.colors.borderDefault
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(4.dp)
            .fillMaxWidth()
            .height(50.dp)
    ) {
        /*The indicator width = 1/3 of the track. With BiasAlignment and a child
 *       that's fillMaxWidth(1f/3f) of the inner area, bias -1/0/1 lands it in the
 *       three thirds. Give it a glow (amber @35%, dropShadow before its clip).*/
        Box(
            modifier = Modifier
                .fillMaxWidth(0.333f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(10.dp))
                .background(ForgeTheme.colors.amber)
                .align(BiasAlignment(horizontalBias = targetBias, verticalBias = 0f))
        ) {

        }

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(Priority.LOW, Priority.MED, Priority.HIGH).forEach { priority ->

                val selectedColor by animateColorAsState(
                    targetValue = if (value == selected) ForgeTheme.colors.fgOnAmber else ForgeTheme.colors.fgSecondary,
                    animationSpec = tween(200, easing = EaseOut),
                    label = "selected color"
                )

                Row(
                    modifier = Modifier
                        .height(42.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = true,
                            onClick = {
                                onChange(priority)
                                selected = priority
                            }
                        )
                        .weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = priority.label, style = TextStyle(
                            fontFamily = ForgeFonts.spaceGrotesk,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W600,
                            color = selectedColor
                        )
                    )
                }
            }
        }
    }
}