package com.development.forge.core.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val OnForgeOnEdit: @Composable (onClick: () -> Unit) -> Unit = { onClick ->
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp)
    ) {
        Icon(
            Icons.Filled.Edit,
            contentDescription = "Edit",
            modifier = Modifier.size(24.dp),
            tint = Color(0xFFF5EDDE)
        )
    }
}

@Preview
@Composable
private fun OnForgeOnEditPreview() {
    OnForgeOnEdit {

    }
}