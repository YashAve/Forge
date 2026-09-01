package com.development.forge.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.development.forge.core.designsystem.ForgeTheme
import java.util.UUID

data class DynamicSubTaskUiItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val isNew: Boolean = false
)

/**   6b. SUBTASKS
 *     One row per subtask (Row, 12dp gap, 9dp vertical padding, vertically centered):
 *       · a small 7dp filled circle (dot) in colors.fgTertiary
 *       · a NakedInput taking the remaining width (weight 1f), placeholder "Subtask",
 *         bound to that subtask's title. If isNew is true, autofocus it (so a freshly
 *         added row is ready to type in). Pressing Enter/Done adds ANOTHER empty row.
 *       · a delete button on the right: 32dp tappable circle, 17dp close icon.
 *     NOTE: no checkboxes here — subtasks are just a dot plus editable text. You're
 *     writing what the subtasks ARE, not checking them off. That happens in Task Detail.
 *
 *     This row is STATELESS — the title lives in the parent's list, so the parent
 *     always has the current text when Save is tapped.
 */
@Preview
@Composable
fun ForgeSubTaskRow(
    modifier: Modifier = Modifier,
    dynamicSubTaskUiItem: DynamicSubTaskUiItem = DynamicSubTaskUiItem(),
    onTitleChange: (String) -> Unit = { },
    onAddNewSubTask: () -> Unit = { },
    onDeleteSubTask: (DynamicSubTaskUiItem) -> Unit = { }
) {
    Row(
        modifier = modifier.padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .size(7.dp)
                .background(ForgeTheme.colors.fgTertiary)
        )
        ForgeNakedValidationText(
            modifier = Modifier.weight(1f),
            input = dynamicSubTaskUiItem.title,
            placeholder = "Subtask",
            maxLines = 1,
            inputLength = 320,
            inputSize = 14.sp,
            isTakeFocus = dynamicSubTaskUiItem.isNew,
            keyboardActions = KeyboardActions(
                onDone = { onAddNewSubTask() }
            )
        ) { value ->
            onTitleChange(value)
        }
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .size(32.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = true,
                    onClick = { onDeleteSubTask(dynamicSubTaskUiItem) }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Close,
                null,
                Modifier.size(17.dp),
                ForgeTheme.colors.fgTertiary
            )
        }
    }
}