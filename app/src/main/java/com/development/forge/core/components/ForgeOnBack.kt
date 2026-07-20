package com.development.forge.core.components

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.development.forge.core.designsystem.ForgeTheme

val OnForgeOnBack: @Composable (onClick: () -> Unit) -> Unit = { onClick ->
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp).offset(x = (-10).dp)
    ) {
        Icon(
            Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier.size(24.dp),
            tint = ForgeTheme.colors.fgPrimary
        )
    }
}

@Preview
@Composable
private fun OnForgeOnBackPreview() {
    OnForgeOnBack {

    }
}