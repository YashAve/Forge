package com.development.forge.core.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun TaskRow(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Text(text = "TaskRow")
    }
}
