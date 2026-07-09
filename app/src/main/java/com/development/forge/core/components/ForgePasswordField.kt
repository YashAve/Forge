package com.development.forge.core.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun ForgePasswordField(
    value: String = "",
    onValueChange: (String) -> Unit = { },
    modifier: Modifier = Modifier,
    label: String = "Password",
    helper: String? = null,
    error: String? = null,
) {
    var visible by remember { mutableStateOf(false) }
    ForgeTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        leadingIcon = Icons.Default.Lock,
        helper = helper,
        error = error,
        keyboardType = KeyboardType.Password,
        visualTransformation = if (visible) VisualTransformation.None
        else PasswordVisualTransformation(),
        trailing = {
            IconButton(onClick = { visible = !visible }, modifier = Modifier.size(24.dp)) {
                Icon(
                    if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (visible) "Hide password" else "Show password",
                    tint = FgTertiary
                )
            }
        }
    )
}