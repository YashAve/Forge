package com.development.forge.core.components

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme

/*Only rendered when action is not null.
 *   Space Grotesk, 13sp, weight 600, amber — your existing ForgeTextButton*/
@Preview
@Composable
fun ForgeTextButton(
    modifier: Modifier = Modifier,
    text: String = "Submit",
    fontSize: TextUnit = 14.sp,
    onClick: () -> Unit = { },
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 1f,
        animationSpec = tween(120, easing = EaseOut),
        label = "pressAlpha"
    )

    Text(
        text = text,
        color = ForgeTheme.colors.amber,
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .clickable(
                interactionSource = interactionSource,
                indication = null,          // no ripple — the fade IS the feedback
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 6.dp),
        style = TextStyle(
            fontFamily = ForgeFonts.spaceGrotesk,
            fontSize = fontSize,
            fontWeight = FontWeight.W600,
            letterSpacing = (-0.005).em
        )
    )
}