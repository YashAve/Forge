package com.development.forge.feature.tasks.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeFonts.spaceGrotesk
import com.development.forge.core.designsystem.ForgeTheme

@Preview
@Composable
fun ForgeNakedValidationText(
    modifier: Modifier = Modifier,
    input: String = "",
    placeholder: String = "What needs forging?",
    inputSize: TextUnit = 28.sp,
    maxLines: Int = 3,
    isTakeFocus: Boolean = false,
    inputLength: Int = 120,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    onValueChange: (String) -> Unit = { }
) {
    val focusRequester = remember { FocusRequester() }

    val style = TextStyle(
        fontFamily = spaceGrotesk,
        fontSize = inputSize,
        fontWeight = FontWeight.W600,
        letterSpacing = (-0.02).em,
        lineHeight = (1.3).em,
        color = ForgeTheme.colors.fgPrimary
    )

    Column(modifier = modifier) {
        BasicTextField(
            value = input, onValueChange = { value ->
                onValueChange(value.take(inputLength))
            },
            textStyle = style,
            cursorBrush = SolidColor(ForgeTheme.colors.amber),
            decorationBox = { innerTextField ->
                if (input.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = style.copy(color = ForgeTheme.colors.fgTertiary)
                    )
                }
                innerTextField()
            },
            maxLines = maxLines,
            keyboardActions = keyboardActions,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isTakeFocus) Modifier.focusRequester(focusRequester) else Modifier
                )
        )
        if (input.length > (inputLength - 18)) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${input.length}/$inputLength",
                style = TextStyle(
                    fontFamily = ForgeFonts.jetbrainsMono,
                    fontSize = 11.sp,
                    color = if (input.length >= inputLength) ForgeTheme.colors.danger
                    else ForgeTheme.colors.fgTertiary
                ),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}