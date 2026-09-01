package com.development.forge.feature.tasks.ui

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.components.DynamicSubTaskUiItem
import com.development.forge.core.components.ForgeDatePickerDialog
import com.development.forge.core.components.ForgeNakedValidationText
import com.development.forge.core.components.ForgePickerField
import com.development.forge.core.components.ForgePrioritySegmented
import com.development.forge.core.components.ForgeSectionLabel
import com.development.forge.core.components.ForgeSelectionChip
import com.development.forge.core.components.ForgeSubTaskRow
import com.development.forge.core.components.ForgeTaskTopBar
import com.development.forge.core.components.ForgeTimePickerDialog
import com.development.forge.core.components.ForgeToggle
import com.development.forge.core.components.OnForgeOnBack
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme
import com.development.forge.domain.model.Priority
import com.development.forge.domain.model.Subtask
import com.development.forge.feature.tasks.contract.TaskState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import kotlin.time.Instant

/**
 * Add / Edit task screen.
 *
 * Hoisted: title, priority, project, notes, and dueAt live in TaskState and are
 * changed through callbacks. Subtasks are kept LOCAL to this screen (as
 * DynamicSubTaskUiItem, which carries the isNew focus flag and supports inline
 * editing) and are handed up only on save, via onSave(subtasks).
 *
 * Subtask IDs: an existing subtask's id is carried forward from state.subtasks
 * when the screen seeds its local list -- never re-derived. Only a brand-new row
 * (added via "Add subtask") gets a fresh id, generated once at that moment. This
 * is what makes @Upsert update an existing subtask instead of inserting a
 * duplicate with the same title and a new id.
 *
 * dueAt: the date + time pickers are the real source. The quick chips are
 * shortcuts that pre-fill a date. The chip highlight and the picker field text
 * are both derived from state.dueAt, so there is one source of truth.
 */
@Composable
fun AddOrEditTaskScreen(
    modifier: Modifier = Modifier,
    state: TaskState = TaskState(),
    existingProjects: List<String> = emptyList(),
    onUpdateTitle: (String) -> Unit = { },
    onUpdatePriority: (Priority) -> Unit = { },
    onUpdateProject: (String?) -> Unit = { },
    onUpdateDueAt: (Instant?) -> Unit = { },
    onUpdateNotes: (String?) -> Unit = { },
    onSave: (List<Subtask>) -> Unit = { },
    onBack: () -> Unit = { }
) {
    // -- Compose infrastructure --
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    // -- Derived from state --
    val canSave = state.title.trim().isNotEmpty() && !state.isSaving
    val isEditing = state.editingTaskId != null

    val animateSaveColor by animateColorAsState(
        targetValue = if (canSave) ForgeTheme.colors.amber else ForgeTheme.colors.fgDisabled,
        animationSpec = tween(160, easing = EaseOut),
        label = "save animate color"
    )

    // Display text derived from state.dueAt (single source of truth).
    val dateText = state.dueAt?.let { formatDate(it) }
    val timeText = state.dueAt?.let { formatInstantTime(it) }

    // -- Due chips --
    val labels = listOf("Today", "Tomorrow", "This weekend", "Next week")
    val selectedChip = state.dueAt?.let { chipIndexFor(it) } ?: -1

    // -- Picker dialog visibility (UI-only) --
    var isShowDate by rememberSaveable { mutableStateOf(false) }
    var isShowTime by rememberSaveable { mutableStateOf(false) }

    // -- Reminder section (UI-only, not persisted) --
    var remind by rememberSaveable { mutableStateOf(false) }
    var remindStart by rememberSaveable { mutableStateOf("9:00 AM") }
    var remindEnd by rememberSaveable { mutableStateOf("6:00 PM") }
    var isShowRemindStart by rememberSaveable { mutableStateOf(false) }
    var isShowRemindEnd by rememberSaveable { mutableStateOf(false) }

    // -- More details toggle --
    var expanded by rememberSaveable { mutableStateOf(false) }
    val angle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(200, easing = EaseOut)
    )

    // -- Subtasks: LOCAL UI state. Seeded once from the loaded task, ID preserved. --
    val subTaskLists = remember(state.editingTaskId) {
        mutableStateListOf<DynamicSubTaskUiItem>().apply {
            state.subtasks.forEach { subtask ->
                add(DynamicSubTaskUiItem(id = subtask.id, title = subtask.title))
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(modifier = modifier) {
        ForgeTaskTopBar(
            label = if (isEditing) "Edit Task" else "Add Task",
            onStart = {
                OnForgeOnBack {
                    onBack()
                }
            },
            onEnd = {
                Text(
                    text = "Save",
                    style = TextStyle(
                        fontFamily = ForgeFonts.spaceGrotesk,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.W700,
                        color = animateSaveColor
                    ),
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = interactionSource,
                        enabled = canSave,
                        onClick = {
                            onSave(
                                subTaskLists
                                    .mapNotNull { item ->
                                        val trimmed = item.title.trim()
                                        if (trimmed.isEmpty()) return@mapNotNull null
                                        Subtask(id = item.id, title = trimmed)
                                    }
                            )
                        }
                    )
                )
            }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 18.dp, bottom = 36.dp)
                .verticalScroll(scrollState)
        ) {
            ForgeNakedValidationText(
                input = state.title,
                placeholder = "What needs forging?",
                maxLines = 3,
                isTakeFocus = true,
                inputLength = 120
            ) { value ->
                onUpdateTitle(value.take(120))
            }
            Spacer(modifier = Modifier.height(36.dp))
            ForgeSectionLabel(label = "DUE")
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                labels.forEachIndexed { index, value ->
                    ForgeSelectionChip(
                        label = value,
                        selected = index == selectedChip
                    ) {
                        // Tapping a selected chip clears the date; otherwise sets it.
                        onUpdateDueAt(
                            if (index == selectedChip) null else instantForChip(index)
                        )
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
                    value = dateText,
                    placeholder = "Pick a date"
                ) {
                    isShowDate = true
                }
                ForgePickerField(
                    icon = Icons.Outlined.Schedule,
                    value = timeText,
                    placeholder = "Time"
                ) {
                    isShowTime = true
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
            ForgeSectionLabel(label = "Priority")
            Spacer(modifier = Modifier.height(10.dp))
            ForgePrioritySegmented(
                value = state.priority
            ) { value ->
                onUpdatePriority(value)
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
                    // -- PROJECT --
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
                                selected = state.project == projectName
                            ) {
                                onUpdateProject(
                                    if (state.project == projectName) null else projectName
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    ForgeNakedValidationText(
                        input = state.project ?: "",
                        placeholder = "Or type a new project",
                        inputSize = 18.sp,
                        maxLines = 1,
                        isTakeFocus = false,
                        inputLength = 30
                    ) { value ->
                        onUpdateProject(value.take(30).ifEmpty { null })
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
                        label = "Subtasks${if (subTaskLists.isNotEmpty()) " . ${subTaskLists.size}" else ""}"
                    )

                    val onAddSubTask: () -> Unit = {
                        subTaskLists.indices.forEach { i ->
                            subTaskLists[i] = subTaskLists[i].copy(isNew = false)
                        }
                        // Fresh row -> fresh id, generated once, right here.
                        subTaskLists.add(
                            DynamicSubTaskUiItem(id = UUID.randomUUID().toString(), title = "", isNew = true)
                        )
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

                    // -- REMINDER (UI-only, not persisted) --
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

                    // -- NOTES --
                    Spacer(modifier = Modifier.height(30.dp))
                    ForgeSectionLabel(label = "Notes")
                    Spacer(modifier = Modifier.height(12.dp))
                    ForgeNakedValidationText(
                        input = state.notes ?: "",
                        placeholder = "Anything else worth noting",
                        inputSize = 15.sp,
                        maxLines = 4,
                        isTakeFocus = false,
                        inputLength = 500
                    ) { value ->
                        onUpdateNotes(value.take(500).ifEmpty { null })
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

    // -- Picker dialogs, overlaid outside the scroll area --
    if (isShowDate) ForgeDatePickerDialog(
        onDateSelected = { millis ->
            onUpdateDueAt(combineDateTime(dateMillis = millis, existing = state.dueAt))
            isShowDate = false
        },
        onDismiss = { isShowDate = false }
    )
    if (isShowTime) ForgeTimePickerDialog(
        onTimeSelected = { hour, minute ->
            onUpdateDueAt(combineTime(hour = hour, minute = minute, existing = state.dueAt))
            isShowTime = false
        },
        onDismiss = { isShowTime = false }
    )
    if (isShowRemindStart) ForgeTimePickerDialog(
        onTimeSelected = { hour, minute ->
            remindStart = formatTime(hour, minute)
            isShowRemindStart = false
        },
        onDismiss = { isShowRemindStart = false }
    )
    if (isShowRemindEnd) ForgeTimePickerDialog(
        onTimeSelected = { hour, minute ->
            remindEnd = formatTime(hour, minute)
            isShowRemindEnd = false
        },
        onDismiss = { isShowRemindEnd = false }
    )
}

// -- dueAt helpers -----------------------------------------------------------

/** Merge a newly picked calendar date into an existing instant, keeping its time. */
private fun combineDateTime(dateMillis: Long, existing: Instant?): Instant {
    val picked = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = dateMillis
    }
    val result = Calendar.getInstance()
    if (existing != null) result.timeInMillis = existing.toEpochMilliseconds()
    result.set(Calendar.YEAR, picked.get(Calendar.YEAR))
    result.set(Calendar.MONTH, picked.get(Calendar.MONTH))
    result.set(Calendar.DAY_OF_MONTH, picked.get(Calendar.DAY_OF_MONTH))
    return Instant.fromEpochMilliseconds(result.timeInMillis)
}

/** Merge a newly picked time into an existing instant, keeping its date. */
private fun combineTime(hour: Int, minute: Int, existing: Instant?): Instant {
    val result = Calendar.getInstance()
    if (existing != null) result.timeInMillis = existing.toEpochMilliseconds()
    result.set(Calendar.HOUR_OF_DAY, hour)
    result.set(Calendar.MINUTE, minute)
    result.set(Calendar.SECOND, 0)
    result.set(Calendar.MILLISECOND, 0)
    return Instant.fromEpochMilliseconds(result.timeInMillis)
}

/** A concrete instant for each quick chip, at 9:00 AM local time. */
private fun instantForChip(index: Int): Instant {
    val c = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 9)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    when (index) {
        0 -> Unit                                   // Today
        1 -> c.add(Calendar.DAY_OF_MONTH, 1)        // Tomorrow
        2 -> {                                       // This weekend -> upcoming Saturday
            while (c.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
                c.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        3 -> c.add(Calendar.DAY_OF_MONTH, 7)        // Next week
    }
    return Instant.fromEpochMilliseconds(c.timeInMillis)
}

/** Which chip, if any, exactly matches the stored instant's day (at 9 AM). */
private fun chipIndexFor(dueAt: Instant): Int {
    for (i in 0..3) {
        val chip = Calendar.getInstance().apply { timeInMillis = instantForChip(i).toEpochMilliseconds() }
        val due = Calendar.getInstance().apply { timeInMillis = dueAt.toEpochMilliseconds() }
        val sameDay = chip.get(Calendar.YEAR) == due.get(Calendar.YEAR) &&
                chip.get(Calendar.DAY_OF_YEAR) == due.get(Calendar.DAY_OF_YEAR)
        if (sameDay) return i
    }
    return -1
}

private fun formatDate(instant: Instant): String {
    val c = Calendar.getInstance().apply { timeInMillis = instant.toEpochMilliseconds() }
    return SimpleDateFormat("EEE, MMM d", Locale.ENGLISH).format(c.time)
}

private fun formatInstantTime(instant: Instant): String {
    val c = Calendar.getInstance().apply { timeInMillis = instant.toEpochMilliseconds() }
    return formatTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
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