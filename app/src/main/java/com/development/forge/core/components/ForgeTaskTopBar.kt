package com.development.forge.core.components

import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.designsystem.ForgeFonts

@Preview
@Composable
fun ForgeTaskTopBar(
    modifier: Modifier = Modifier,
    label: String = "All Tasks",
    onStart: @Composable () -> Unit = { },
    onEnd: @Composable () -> Unit = { }
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp),
        horizontalArrangement = SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            onStart()
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.W600,
                    letterSpacing = (-0.005).em,
                    color = Color(0xFFF5EDDE)
                )
            )
        }
        onEnd()
    }
}