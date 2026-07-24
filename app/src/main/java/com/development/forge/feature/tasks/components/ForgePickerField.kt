package com.development.forge.feature.tasks.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme

/**
 * ── ForgePickerField(icon, value: String?, placeholder, onClick) ── NEW small component
 *   A clickable pill: height 56dp, corner 16dp, padding 0/16dp, background
 *   colors.bgRaised, 1.5dp border colors.borderDefault. A Row (gap 10dp):
 *     - the icon: 20dp, colors.fgTertiary
 *     - the value text (weight 1f): JetBrains Mono, 14sp, W500, single line,
 *       ellipsis. Colour colors.fgPrimary when value present, else colors.fgTertiary
 *       (showing the placeholder). No ripple; it opens a picker dialog on tap.*/
@Preview
@Composable
fun ForgePickerField(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.CalendarMonth,
    value: String? = null,
    placeholder: String = "Pick a date",
    onClick: () -> Unit = { }
) {
    Box(
        modifier = modifier
            .wrapContentWidth()
            .height(38.dp)
            .border(
                border = BorderStroke(
                    width = (1.5).dp,
                    color = ForgeTheme.colors.borderDefault
                ),
                shape = RoundedCornerShape(percent = 100)
            )
            .clip(RoundedCornerShape(percent = 100))
            .background(ForgeTheme.colors.bgRaised)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = true,
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                Modifier.size(20.dp),
                ForgeTheme.colors.fgTertiary
            )
            Text(text = value?.let { value } ?: placeholder, style = TextStyle(
                fontFamily = ForgeFonts.jetbrainsMono,
                fontSize = 14.sp,
                fontWeight = FontWeight.W500,
                color = value?.let { ForgeTheme.colors.fgPrimary } ?: ForgeTheme.colors.fgTertiary
            ), overflow = TextOverflow.Ellipsis)
        }
    }
}