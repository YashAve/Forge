package com.development.forge.core.components.canvas

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.development.forge.core.components.Amber

@Preview
@Composable
fun ForgePaginationDots(
    modifier: Modifier = Modifier,
    totalSteps: Int = 3,
    currentStep: Int = 0
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index == currentStep
            val width by animateDpAsState(
                targetValue = if (isActive) 24.dp else 8.dp,
                animationSpec = tween(200, easing = EaseOut),
                label = "width"
            )
            val background by animateColorAsState(
                targetValue = if (isActive) Amber
                else Color(0xFFF5EDDE).copy(alpha = 0.18f),
                animationSpec = tween(200, easing = EaseOut),
                label = "background"
            )

            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(background)
            )
        }
    }
}