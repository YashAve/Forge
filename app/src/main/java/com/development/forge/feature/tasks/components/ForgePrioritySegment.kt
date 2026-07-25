package com.development.forge.feature.tasks.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme
import com.development.forge.domain.model.Priority

@Preview
@Composable
fun ForgePrioritySegmented(
    modifier: Modifier = Modifier,
    value: Priority = Priority.MED,
    onChange: (Priority) -> Unit = { }
) {
    // indicator slides to the selected segment via BiasAlignment
    // -1f = Low (left), 0f = Med (center), 1f = High (right)
    val bias by animateFloatAsState(
        targetValue = when (value) {
            Priority.LOW -> -1f
            Priority.MED -> 0f
            Priority.HIGH -> 1f
        },
        animationSpec = tween(
            durationMillis = 240,
            easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)  // ease-spring — overshoots
        ),
        label = "indicatorBias"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ForgeTheme.colors.bgRaised)
            .border(
                border = BorderStroke(1.5.dp, ForgeTheme.colors.borderDefault),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(4.dp)
            .height(50.dp)
    ) {
        // ── sliding amber indicator — drawn first so segments sit on top ──
        Box(
            modifier = Modifier
                .fillMaxWidth(1f / 3f)
                .height(42.dp)
                .align(BiasAlignment(horizontalBias = bias, verticalBias = 0f))
                .dropShadow(
                    shape = RoundedCornerShape(10.dp),
                    shadow = Shadow(
                        color = ForgeTheme.colors.amber.copy(alpha = 0.35f),
                        offset = DpOffset(0.dp, 0.dp),
                        radius = 14.dp
                    )
                )
                .clip(RoundedCornerShape(10.dp))
                .background(ForgeTheme.colors.amber)
        )

        // ── three segment labels — drawn on top of the indicator ──
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Priority.LOW  to "Low",
                Priority.MED  to "Medium",
                Priority.HIGH to "High"
            ).forEach { (priority, label) ->
                val isActive = priority == value

                val textColor by animateColorAsState(
                    targetValue = if (isActive) ForgeTheme.colors.fgOnAmber
                    else ForgeTheme.colors.fgSecondary,
                    animationSpec = tween(200, easing = EaseOut),
                    label = "text_${priority.name}"
                )
                val dotColor by animateColorAsState(
                    targetValue = if (isActive) ForgeTheme.colors.fgOnAmber
                    else ForgeTheme.colors.fgTertiary,
                    animationSpec = tween(200, easing = EaseOut),
                    label = "dot_${priority.name}"
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onChange(priority) }
                        ),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = label,
                        style = TextStyle(
                            fontFamily = ForgeFonts.spaceGrotesk,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W600,
                            color = textColor
                        )
                    )
                }
            }
        }
    }
}