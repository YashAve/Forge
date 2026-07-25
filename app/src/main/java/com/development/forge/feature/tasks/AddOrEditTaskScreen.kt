package com.development.forge.feature.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.components.ForgeSectionLabel
import com.development.forge.core.components.ForgeTaskTopBar
import com.development.forge.core.components.ForgeToggle
import com.development.forge.core.components.OnForgeOnBack
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme
import com.development.forge.domain.model.Priority
import com.development.forge.domain.model.Task
import com.development.forge.domain.model.tasks
import com.development.forge.feature.tasks.components.DynamicSubTaskUiItem
import com.development.forge.feature.tasks.components.ForgeDatePickerDialog
import com.development.forge.feature.tasks.components.ForgeNakedValidationText
import com.development.forge.feature.tasks.components.ForgePickerField
import com.development.forge.feature.tasks.components.ForgePrioritySegmented
import com.development.forge.feature.tasks.components.ForgeSelectionChip
import com.development.forge.feature.tasks.components.ForgeSubTaskRow
import com.development.forge.feature.tasks.components.ForgeTimePickerDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

// ... your full KDoc block stays exactly as it is ...

@Preview
@Composable
fun AddOrEditTaskScreen(
    modifier: Modifier = Modifier,
    editingTask: Task? = null,
    existingProjects: List<String> = listOf(
        *tasks.mapNotNull { it.project }.distinct().toTypedArray()
    ),
    onSave: () -> Unit = { },
    onBack: () -> Unit = { }
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var title by rememberSaveable { mutableStateOf(editingTask?.title ?: "") }
    var notes by rememberSaveable { mutableStateOf(editingTask?.notes ?: "") }
    var project by rememberSaveable { mutableStateOf(editingTask?.project ?: "") }
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val canSave by remember {
        derivedStateOf {
            title.trim().isNotEmpty()
        }
    }
    val animateSaveColor by animateColorAsState(
        targetValue = if (canSave) ForgeTheme.colors.amber else ForgeTheme.colors.fgDisabled,
        animationSpec = tween(160, easing = EaseOut),
        label = "save animate color"
    )

    val labels = listOf("Today", "Tomorrow", "This weekend", "Next week")
    val selections = remember {
        mutableStateListOf(
            *(editingTask?.due?.let { due -> labels.map { it == due } }
                ?: List(labels.size) { false }).toTypedArray()
        )
    }

    var date: String? by rememberSaveable { mutableStateOf(null) }
    var time: String? by rememberSaveable { mutableStateOf(null) }
    var isShowDate by rememberSaveable { mutableStateOf(false) }
    var isShowTime by rememberSaveable { mutableStateOf(false) }

    // reminder window
    var remind by rememberSaveable { mutableStateOf(false) }
    var remindStart by rememberSaveable { mutableStateOf("9:00 AM") }
    var remindEnd by rememberSaveable { mutableStateOf("6:00 PM") }
    var isShowRemindStart by rememberSaveable { mutableStateOf(false) }
    var isShowRemindEnd by rememberSaveable { mutableStateOf(false) }

    var priority by rememberSaveable { mutableStateOf(Priority.HIGH) }

    var expanded by rememberSaveable { mutableStateOf(false) }
    val angle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(200, easing = EaseOut)
    )

    val subTaskLists = remember(editingTask) {
        mutableStateListOf<DynamicSubTaskUiItem>().apply {
            editingTask?.subtasks?.map { subTask ->
                DynamicSubTaskUiItem(title = subTask.title)
            }?.let { mappedList -> addAll(mappedList) }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(modifier = modifier) {
        ForgeTaskTopBar(
            label = editingTask?.let { "Edit Task" } ?: "Add Task",
            onStart = {
                OnForgeOnBack {
                    onBack()
                }
            },
            onEnd = {
                Text(
                    text = "Save", style = TextStyle(
                        fontFamily = ForgeFonts.spaceGrotesk,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.W700,
                        color = animateSaveColor
                    ),
                    modifier = Modifier.clickable(
                        indication = null, interactionSource = interactionSource,
                        enabled = canSave,
                        onClick = {
                            onSave()
                        }
                    )
                )
            })
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 18.dp, bottom = 36.dp)
                .verticalScroll(scrollState)
        ) {
            ForgeNakedValidationText(
                input = title,
                placeholder = "What needs forging?",
                maxLines = 3,
                isTakeFocus = true,
                inputLength = 120
            ) { value ->
                title = value.take(120)
            }
            Spacer(modifier = Modifier.height(36.dp))
            ForgeSectionLabel(
                label = "DUE"
            )
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                labels.forEachIndexed { index, value ->
                    ForgeSelectionChip(
                        label = value,
                        selected = selections[index]
                    ) {
                        val wasSelected = selections[index]
                        selections.indices.forEach { i -> selections[i] = false }
                        if (!wasSelected) selections[index] = true
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ForgePickerField(
                    icon = Icons.Outlined.CalendarMonth,
                    value = date,
                    placeholder = "Pick a date"
                ) {
                    isShowDate = true
                }
                ForgePickerField(
                    icon = Icons.Outlined.Schedule,
                    value = time,
                    placeholder = "Time"
                ) {
                    isShowTime = true
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
            ForgeSectionLabel(label = "Priority")
            Spacer(modifier = Modifier.height(10.dp))
            ForgePrioritySegmented(
                value = priority
            ) { value ->
                priority = value
            }
            Spacer(modifier = Modifier.height(34.dp))
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ForgeTheme.colors.borderSubtle)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = true,
                        onClick = {
                            expanded = !expanded
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ForgeSectionLabel(label = "MORE DETAILS", modifier = Modifier.weight(1f))
                Icon(
                    Icons.Outlined.KeyboardArrowDown,
                    null,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            rotationZ = angle
                        },
                    ForgeTheme.colors.fgTertiary
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(320, easing = EaseOut)),
                exit = shrinkVertically(tween(320, easing = EaseOut))
            ) {
                Column {
                    // ── PROJECT ──────────────────────────────────────────
                    Spacer(modifier = Modifier.height(24.dp))
                    ForgeSectionLabel(label = "Project")
                    Spacer(modifier = Modifier.height(16.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        existingProjects.forEach { projectName ->
                            ForgeSelectionChip(
                                label = projectName,
                                selected = project == projectName
                            ) {
                                project = if (project == projectName) "" else projectName
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    ForgeNakedValidationText(
                        input = project,
                        placeholder = "Or type a new project",
                        inputSize = 18.sp,
                        maxLines = 1,
                        isTakeFocus = false,
                        inputLength = 30
                    ) { value ->
                        project = value.take(30)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(ForgeTheme.colors.borderDefault)
                    )

                    Spacer(modifier = Modifier.height(30.dp))
                    ForgeSectionLabel(
                        label = "Subtasks${if (subTaskLists.isNotEmpty()) " · ${subTaskLists.size}" else ""}"
                    )

                    val onAddSubTask: () -> Unit = {
                        // clear the isNew flag on every existing row so only the
                        // newest one grabs focus
                        subTaskLists.indices.forEach { i ->
                            subTaskLists[i] = subTaskLists[i].copy(isNew = false)
                        }
                        subTaskLists.add(DynamicSubTaskUiItem(title = "", isNew = true))
                        scope.launch {
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    }
                    val onDeleteSubTask: (DynamicSubTaskUiItem) -> Unit = { subtask ->
                        subTaskLists.remove(subtask)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Column {
                        subTaskLists.forEachIndexed { index, subTask ->
                            ForgeSubTaskRow(
                                dynamicSubTaskUiItem = subTask,
                                onTitleChange = { newTitle ->
                                    // write the typed text back into the list so the
                                    // parent always holds the current value
                                    subTaskLists[index] = subTask.copy(title = newTitle)
                                },
                                onAddNewSubTask = onAddSubTask,
                                onDeleteSubTask = onDeleteSubTask
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                enabled = true,
                                onClick = { onAddSubTask() }
                            )
                            .padding(vertical = 9.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Add,
                            null,
                            Modifier.size(19.dp),
                            ForgeTheme.colors.amber
                        )
                        Text(
                            text = "Add subtask",
                            style = TextStyle(
                                fontFamily = ForgeFonts.spaceGrotesk,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W600,
                                color = ForgeTheme.colors.amber
                            )
                        )
                    }

                    /**   6c. REMINDER (30dp gap above)
                     *     A Row with the two ends pushed apart: a ForgeSectionLabel "Remind me"
                     *     on the left, and a ForgeToggle on the right bound to `remind`.
                     *     BELOW that, wrapped in its OWN AnimatedVisibility (visible only when
                     *     remind == true), the from/to pickers and the explaining microcopy.*/
                    Spacer(modifier = Modifier.height(30.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ForgeSectionLabel(
                            label = "Remind me",
                            modifier = Modifier.weight(1f)
                        )
                        ForgeToggle(
                            checked = remind,
                            onChange = { remind = it }
                        )
                    }
                    AnimatedVisibility(
                        visible = remind,
                        enter = expandVertically(tween(280, easing = EaseOut)),
                        exit = shrinkVertically(tween(280, easing = EaseOut))
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ForgePickerField(
                                    icon = Icons.Outlined.Notifications,
                                    value = remindStart,
                                    placeholder = "From"
                                ) {
                                    isShowRemindStart = true
                                }
                                Text(
                                    text = "TO",
                                    style = TextStyle(
                                        fontFamily = ForgeFonts.jetbrainsMono,
                                        fontSize = 11.sp,
                                        letterSpacing = (0.1).em,
                                        color = ForgeTheme.colors.fgTertiary
                                    )
                                )
                                ForgePickerField(
                                    icon = Icons.Outlined.NotificationsActive,
                                    value = remindEnd,
                                    placeholder = "To"
                                ) {
                                    isShowRemindEnd = true
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Forge only nudges you inside this window. " +
                                        "No pings before $remindStart or after $remindEnd.",
                                style = TextStyle(
                                    fontFamily = ForgeFonts.spaceGrotesk,
                                    fontSize = 13.sp,
                                    lineHeight = (1.45).em,
                                    color = ForgeTheme.colors.fgTertiary
                                )
                            )
                        }
                    }

                    /**   6d. NOTES (30dp gap above)
                     *     A ForgeSectionLabel "Notes". Then a NakedInput (normal size), placeholder
                     *     "Anything else worth noting", bound to `notes`, with 8dp bottom padding and
                     *     a 1dp bottom line — same single-underline look as the project text field.*/
                    Spacer(modifier = Modifier.height(30.dp))
                    ForgeSectionLabel(label = "Notes")
                    Spacer(modifier = Modifier.height(12.dp))
                    ForgeNakedValidationText(
                        input = notes,
                        placeholder = "Anything else worth noting",
                        inputSize = 15.sp,
                        maxLines = 4,
                        isTakeFocus = false,
                        inputLength = 500
                    ) { value ->
                        notes = value.take(500)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(ForgeTheme.colors.borderDefault)
                    )
                }
            }
        }
    }

    /**
     * 7. THE PICKER DIALOGS — moved OUTSIDE the scrolling Column so they overlay
     *    the whole screen rather than being clipped inside the scroll area.
     */
    if (isShowDate) ForgeDatePickerDialog(
        onDateSelected = { millis ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = millis
            }
            date = SimpleDateFormat("EEE, MMM d", Locale.ENGLISH).format(calendar.time)
            isShowDate = false
        },
        onDismiss = { isShowDate = false }
    )
    if (isShowTime) ForgeTimePickerDialog(
        onTimeSelected = { hour, minute ->
            time = formatTime(hour, minute)
        },
        onDismiss = { isShowTime = false }
    )
    if (isShowRemindStart) ForgeTimePickerDialog(
        onTimeSelected = { hour, minute ->
            remindStart = formatTime(hour, minute)
        },
        onDismiss = { isShowRemindStart = false }
    )
    if (isShowRemindEnd) ForgeTimePickerDialog(
        onTimeSelected = { hour, minute ->
            remindEnd = formatTime(hour, minute)
        },
        onDismiss = { isShowRemindEnd = false }
    )
}

// converts the picker's 24h hour + minute into "6:00 PM" style display text
private fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$displayHour:${minute.toString().padStart(2, '0')} $amPm"
}