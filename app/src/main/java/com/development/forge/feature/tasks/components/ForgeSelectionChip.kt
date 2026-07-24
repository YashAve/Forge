package com.development.forge.feature.tasks.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme

/** ═══════════════════════════════════════════════════════════════
 * 3. DUE GROUP (28dp top margin)
 * ═══════════════════════════════════════════════════════════════
 * A ForgeSectionLabel "Due", 12dp gap, then:
 *   a) QUICK-DATE CHIPS — a wrapping Row (FlowRow), 8dp gap, four chips:
 *      "Today", "Tomorrow", "This weekend", "Next week".
 *      Each chip: padding 8dp vertical / 14dp horizontal, fully rounded (999),
 *      Space Grotesk 13sp W600.
 *        - SELECTED: background colors.amber, text colors.fgOnAmber, no border
 *        - UNSELECTED: background colors.bgRaised, text colors.fgSecondary,
 *          1.5dp border colors.borderDefault
 *      Tapping a chip sets dueDate to that preset; tapping the ACTIVE chip again
 *      clears it (toggle). Only one active at a time. Transition 140ms ease-out.
 *      (Use androidx.compose.foundation.layout.FlowRow — the wrapping Row.)
 *   b) 10dp gap, then a Row of two PICKER FIELDS (gap 10dp):
 *      - date picker (weight 1.4f): ForgePickerField, icon `calendar_month`,
 *        shows the formatted date or "Pick a date"
 *      - time picker (weight 1f): ForgePickerField, icon `schedule`, shows the
 *        time or "Time"*/
@Preview
@Composable
fun ForgeSelectionChip(
    modifier: Modifier = Modifier,
    label: String = "Today",
    selected: Boolean = false,
    onClick: () -> Unit = { }
) {
    val chipColor by animateColorAsState(
        targetValue = if (selected) ForgeTheme.colors.amber else ForgeTheme.colors.bgRaised,
        animationSpec = tween(140, easing = EaseOut),
        label = "chip color"
    )
    val chipBorder by animateDpAsState(
        targetValue = if (selected) 0.dp else (1.5).dp,
        animationSpec = tween(140, easing = EaseOut),
        label = "chip border"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 100))
            .background(chipColor)
            .border(
                border = BorderStroke(
                    width = chipBorder,
                    color = ForgeTheme.colors.borderDefault
                ),
                shape = RoundedCornerShape(percent = 100)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = true,
                onClick = {
                    onClick()
                }
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label, style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk,
                fontSize = 13.sp,
                fontWeight = FontWeight.W600,
                color = if (selected) ForgeTheme.colors.fgOnAmber else ForgeTheme.colors.fgSecondary
            )
        )
    }
}