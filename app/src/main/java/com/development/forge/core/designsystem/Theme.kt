package com.development.forge.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun ForgeTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalForgeColors provides if (darkTheme) ForgeDarkColors else ForgeLightColors
    ) {
        MaterialTheme(
            typography = Typography,
            content = content
        )
    }
}
