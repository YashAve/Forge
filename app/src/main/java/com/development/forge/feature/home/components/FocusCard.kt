package com.development.forge.feature.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun FocusCard(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Text(text = "FocusCard")
    }
}
