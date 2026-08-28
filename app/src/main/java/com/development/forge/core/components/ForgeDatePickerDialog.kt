package com.development.forge.core.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme

@Preview
@Composable
fun ForgeDatePickerDialog(
    modifier: Modifier = Modifier,
    onDateSelected: (Long) -> Unit = { },
    onDismiss: () -> Unit = { }
) {
    val datePickerState = rememberDatePickerState()
    val datePickerColorDefaults = DatePickerDefaults.colors(
        containerColor = ForgeTheme.colors.bgElev,
        titleContentColor = ForgeTheme.colors.fgTertiary,
        headlineContentColor = ForgeTheme.colors.fgPrimary,
        weekdayContentColor = ForgeTheme.colors.fgTertiary,
        subheadContentColor = ForgeTheme.colors.fgSecondary,
        navigationContentColor = ForgeTheme.colors.fgSecondary,
        yearContentColor = ForgeTheme.colors.fgPrimary,
        currentYearContentColor = ForgeTheme.colors.amber,
        selectedYearContentColor = ForgeTheme.colors.fgOnAmber,
        selectedYearContainerColor = ForgeTheme.colors.amber,
        dayContentColor = ForgeTheme.colors.fgPrimary,
        selectedDayContentColor = ForgeTheme.colors.fgOnAmber,
        selectedDayContainerColor = ForgeTheme.colors.amber,
        todayContentColor = ForgeTheme.colors.amber,
        todayDateBorderColor = ForgeTheme.colors.amber,
        dayInSelectionRangeContentColor = ForgeTheme.colors.fgOnAmber,
        dayInSelectionRangeContainerColor = ForgeTheme.colors.amber.copy(alpha = 0.16f)
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis!!)
                onDismiss()
            }) {
                Text(
                    "OK",
                    style = TextStyle(
                        fontFamily = ForgeFonts.spaceGrotesk,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W600,
                        color = ForgeTheme.colors.amber
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel",
                    style = TextStyle(
                        fontFamily = ForgeFonts.spaceGrotesk,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W600,
                        color = ForgeTheme.colors.fgSecondary
                    )
                )
            }
        },
        shape = RoundedCornerShape(28.dp),
        colors = datePickerColorDefaults,
        modifier = modifier
    ) {
        DatePicker(
            state = datePickerState,
            colors = datePickerColorDefaults
        )
    }
}