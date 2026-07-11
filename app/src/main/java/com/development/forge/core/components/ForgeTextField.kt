package com.development.forge.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp

// TODO: move these to core/designsystem as your real token objects.
private val BgRaised = Color(0xFF1C180F)
val Amber = Color(0xFFF58A00)
val Amber500 = Color(0xFFD46E00)
private val FgPrimary = Color(0xFFF5EDDE)
private val FgSecondary = Color(0xFFB5A990)
val FgTertiary = Color(0xFF7A7060)
private val BorderStrong = Color(0xFFF5EDDE).copy(alpha = 0.18f)
private val Danger = Color(0xFFE5484D)   // replace with the design's danger token
val DeepEarthBrown = Color(0xFF3A3020)
val DarkEspresso = Color(0xFF1A1207)
val AgedBrass = Color(0xFF4A4338)

@Preview
@Composable
fun ForgeTextField(
    value: String = "",
    onValueChange: (String) -> Unit = { },
    modifier: Modifier = Modifier,
    label: String? = null,
    leadingIcon: ImageVector? = null,
    helper: String? = null,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: @Composable (() -> Unit)? = null,
) {
    // Focus comes from the field's own interaction stream — the single source of truth.
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val floated = focused || value.isNotEmpty()

    // ONE 0→1 value drives the whole label transition (position + size + weight).
    val progress by animateFloatAsState(if (floated) 1f else 0f, label = "labelFloat")

    val borderColor by animateColorAsState(
        targetValue = when {
            error != null -> Danger
            focused -> Amber
            else -> BorderStrong
        },
        label = "border"
    )
    val iconColor = if (focused) Amber else FgTertiary
    val labelColor = if (focused) Amber else FgSecondary
    val shape = RoundedCornerShape(16.dp)

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)                      // fixed — single-line field is always 64
                .drawBehind { /* focus ring, unchanged */ }
                .background(BgRaised, shape)
                .border(1.5.dp, borderColor, shape)
                .padding(horizontal = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Icon(leadingIcon, null, Modifier.size(22.dp), tint = iconColor)
                    Spacer(Modifier.width(14.dp))
                }

                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()            // ← THE FIX: offsets now anchor to the field
                ) {
                    if (label != null) {
                        Text(
                            text = label,
                            color = labelColor,
                            fontSize = lerp(16.sp, 11.sp, progress),
                            fontWeight = if (floated) FontWeight.W600 else FontWeight.W500,
                            letterSpacing = if (floated) 0.06.em else 0.sp,
                            // two absolute positions, measured from the FIELD's top:
                            // rest = 20.dp (visually centered), floated = 10.dp
                            modifier = Modifier.offset(y = lerp(20.dp, 10.dp, progress))
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(if (label != null) Alignment.TopStart else Alignment.CenterStart)
                            .padding(top = if (label != null) 28.dp else 0.dp),   // FIXED input position
                        textStyle = TextStyle(color = FgPrimary, fontSize = 16.sp),
                        singleLine = true,
                        cursorBrush = SolidColor(Amber),
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                        visualTransformation = visualTransformation,
                        interactionSource = interactionSource,
                    )
                }

                if (trailing != null) {
                    Spacer(Modifier.width(8.dp))
                    trailing()
                }
            }
        }

        // Supporting line: error wins over helper.
        (error ?: helper)?.let { supporting ->
            Text(
                text = supporting,
                color = if (error != null) Danger else FgTertiary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 6.dp, start = 18.dp)
            )
        }
    }
}