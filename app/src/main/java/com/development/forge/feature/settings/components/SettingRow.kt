package com.development.forge.feature.settings.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme

@Preview
@Composable
fun SettingRow(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Notifications,
    title: String = "Notifications",
    value: String? = null,
    danger: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) ForgeTheme.colors.bgPress else Color.Transparent,
        animationSpec = tween(120, easing = EaseOut),
        label = "rowBackground"
    )

    val contentColor = if (danger) ForgeTheme.colors.danger else ForgeTheme.colors.fgSecondary
    val titleColor = if (danger) ForgeTheme.colors.danger else ForgeTheme.colors.fgPrimary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)   // transparent when unpressed or non-clickable
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = contentColor
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = title,
            style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk,
                fontSize = 15.sp,
                fontWeight = FontWeight.W500,
                color = titleColor
            ),
            modifier = Modifier.weight(1f)
        )
        value?.let {
            Text(
                text = it,
                style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = 14.sp,
                    color = ForgeTheme.colors.fgTertiary
                )
            )
            Spacer(Modifier.width(14.dp))
        }
        trailing?.invoke()
        if (onClick != null && trailing == null) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = ForgeTheme.colors.fgTertiary
            )
        }
    }
}