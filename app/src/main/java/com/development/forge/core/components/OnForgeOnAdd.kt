package com.development.forge.core.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.development.forge.core.designsystem.ForgeTheme

val OnForgeOnAdd: @Composable (onClick: () -> Unit) -> Unit = { onClick ->
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp)
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = "Add",
            modifier = Modifier.size(24.dp),
            tint = ForgeTheme.colors.fgPrimary
        )
    }
}

@Preview
@Composable
private fun OnForgeOnAddPreview() {
    OnForgeOnAdd {

    }
}