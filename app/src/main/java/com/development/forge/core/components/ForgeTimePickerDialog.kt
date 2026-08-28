package com.development.forge.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ForgeTimePickerDialog(
    modifier: Modifier = Modifier,
    initialHour: Int = 9,
    initialMinute: Int = 0,
    onTimeSelected: (hour: Int, minute: Int) -> Unit = { _, _ -> },
    onDismiss: () -> Unit = { }
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = ForgeTheme.colors.bgElev,
            border = BorderStroke(1.dp, ForgeTheme.colors.borderDefault),
            modifier = modifier
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = ForgeTheme.colors.bgRaised,
                        clockDialSelectedContentColor = ForgeTheme.colors.fgOnAmber,
                        clockDialUnselectedContentColor = ForgeTheme.colors.fgPrimary,
                        selectorColor = ForgeTheme.colors.amber,
                        containerColor = ForgeTheme.colors.bgElev,
                        periodSelectorBorderColor = ForgeTheme.colors.borderDefault,
                        periodSelectorSelectedContainerColor = ForgeTheme.colors.amber,
                        periodSelectorUnselectedContainerColor = ForgeTheme.colors.bgRaised,
                        periodSelectorSelectedContentColor = ForgeTheme.colors.fgOnAmber,
                        periodSelectorUnselectedContentColor = ForgeTheme.colors.fgSecondary,
                        timeSelectorSelectedContainerColor = ForgeTheme.colors.amber.copy(alpha = 0.16f),
                        timeSelectorUnselectedContainerColor = ForgeTheme.colors.bgRaised,
                        timeSelectorSelectedContentColor = ForgeTheme.colors.amberA300,
                        timeSelectorUnselectedContentColor = ForgeTheme.colors.fgPrimary
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Cancel",
                            style = TextStyle(
                                fontFamily = ForgeFonts.spaceGrotesk,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W600,
                                color = ForgeTheme.colors.fgSecondary
                            )
                        )
                    }
                    TextButton(onClick = {
                        onTimeSelected(timePickerState.hour, timePickerState.minute)
                        onDismiss()
                    }) {
                        Text(
                            text = "OK",
                            style = TextStyle(
                                fontFamily = ForgeFonts.spaceGrotesk,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W600,
                                color = ForgeTheme.colors.amber
                            )
                        )
                    }
                }
            }
        }
    }
}