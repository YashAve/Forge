package com.development.forge.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.development.forge.core.designsystem.ForgeTheme

/** ── ForgeToggle(checked: Boolean, onChange: (Boolean) -> Unit) ──
 * A pill switch: 46dp wide, 28dp tall, fully rounded, 3dp inner padding.
 *   - track background (animated 200ms ease-out):
 *       checked → colors.amber ; unchecked → colors.bgPress
 *   - the knob: a 22dp circle, aligned right when checked / left when unchecked.
 *     Animate its position with animateDpAsState OR use Arrangement + an
 *     animated alignment. Knob colour (animated):
 *       checked → colors.fgOnAmber ; unchecked → colors.fgSecondary
 *   - IMPORTANT: the toggle sits inside a clickable SettingRow. Its own tap must
 *     NOT trigger the row. In Compose the toggle's own clickable consumes the tap,
 *     so provide the toggle its own clickable and verify on device it doesn't
 *     also fire the row. (For Notifications/Sound the row has no onClick anyway,
 *     so only the toggle is interactive there — safest.)*/
@Preview
@Composable
fun ForgeToggle(
    modifier: Modifier = Modifier,
    checked: Boolean = true,
    onChange: (Boolean) -> Unit = { }
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) ForgeTheme.colors.amber else ForgeTheme.colors.bgPress,
        animationSpec = tween(200, easing = EaseOut),
        label = "track color"
    )

    val alignmentBias by animateFloatAsState(
        targetValue = if (checked) 1f else -1f,
        animationSpec = tween(200, easing = EaseOut),
        label = "alignment bias"
    )

    val knobColor by animateColorAsState(
        targetValue = if (checked) ForgeTheme.colors.fgOnAmber else ForgeTheme.colors.fgSecondary,
        animationSpec = tween(200, easing = EaseOut),
        label = "knob color"
    )

    Box(
        modifier = modifier
            .width(46.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(percent = 100))
            .background(trackColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = true,
                onClick = {
                    onChange(!checked)
                }
            )
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .align(BiasAlignment(horizontalBias = alignmentBias, verticalBias = 0f))
                .clip(RoundedCornerShape(percent = 100))
                .background(knobColor)
        )
    }
}
