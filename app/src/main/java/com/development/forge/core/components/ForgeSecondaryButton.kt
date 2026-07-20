package com.development.forge.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.R
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme

private val EaseOutForge = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

@Preview
@Composable
fun ForgeSecondaryButton(
    modifier: Modifier = Modifier,
    label: String = "Continue with Google",
    isEnabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = {
        Icon(
            painter = painterResource(R.drawable.google_icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp),          // design: 20.dp
            tint = Color.Unspecified                  // keep the 4 brand colors
        )
    },
    trailingIcon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bg by animateColorAsState(
        targetValue = when {
            !isEnabled -> ForgeTheme.colors.bgRaised     // disabled → default surface (dimmed via alpha below)
            isPressed  -> ForgeTheme.colors.bgPress      // pressed
            else       -> ForgeTheme.colors.bgRaised     // default
        },
        animationSpec = tween(120, easing = EaseOutForge), label = "bg"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = tween(120, easing = EaseOutForge), label = "scale"
    )

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (isEnabled) 1f else 0.4f     // disabled dims to 40%
            }
            // small BLACK shadow at REST, gone while pressed
            .then(
                if (!isPressed && isEnabled)
                    Modifier.dropShadow(
                        shape,
                        Shadow(
                            color = Color.Black.copy(alpha = 0.35f),
                            offset = DpOffset(0.dp, 1.dp),
                            radius = 2.dp
                        )
                    )
                else Modifier
            )
            .clip(shape)
            .background(bg)                                       // fill first
            .border(1.5.dp, ForgeTheme.colors.borderStrong, shape)    // border on top
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isEnabled,
                onClick = onClick
            )
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            leadingIcon?.let {
                it()
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = label,
                color = ForgeTheme.colors.fgPrimary,
                style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600,
                    letterSpacing = (-0.005).em
                )
            )
            trailingIcon?.let {
                Spacer(Modifier.width(10.dp))
                it()
            }
        }
    }
}