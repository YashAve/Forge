package com.development.forge.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.domain.model.Priority

@Preview
@Composable
fun DueChip(
    modifier: Modifier = Modifier, label: String? = "High", overdue: Boolean = false
) {
    val color = if (overdue) Color(0xFFE8624D) else FgTertiary
    label?.let {
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.Schedule,
                null,
                modifier = Modifier.size(13.dp),
                color
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label, style = TextStyle(
                    fontFamily = ForgeFonts.jetbrainsMono,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W500,
                    letterSpacing = (0.04).em,
                    color = color
                )
            )
        }
    }
}

@Preview
@Composable
fun PriorityChip(
    modifier: Modifier = Modifier, level: Priority = Priority.HIGH, dense: Boolean = false
) {
    val backgroundColor = when (level) {
        Priority.HIGH -> Amber
        Priority.MED -> Amber.copy(alpha = 0.16f)
        else -> Color(0xFF251F14)
    }
    val color = when (level) {
        Priority.HIGH -> Color(0xFF1A1207)
        Priority.MED -> Color(0xFFFFAA2E)
        else -> FgTertiary
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (level == Priority.LOW) Modifier.border(
                    1.dp, Color(0xFFF5EDDE).copy(alpha = 0.10f), RoundedCornerShape(8.dp)
                )
                else Modifier
            )
            .background(backgroundColor), contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(
                vertical = if (dense) 2.dp else 4.dp, horizontal = if (dense) 8.dp else 10.dp
            ), verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = level.name, style = TextStyle(
                    fontFamily = ForgeFonts.jetbrainsMono,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.W600,
                    letterSpacing = (0.08).em,
                    color = color
                )
            )
        }
    }
}
