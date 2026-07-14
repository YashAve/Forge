package com.development.forge.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun ForgeTopBar(
    modifier: Modifier = Modifier,
    onStart: (@Composable () -> Unit)? = null,
    onEnd: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (onStart != null || onEnd != null) Arrangement.SpaceBetween else Arrangement.Start
    ) {
        onStart?.let { onStart() }
        ForgeWordmark()
        onEnd?.let { onEnd() }
    }
}