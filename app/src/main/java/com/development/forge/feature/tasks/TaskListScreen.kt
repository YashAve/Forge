package com.development.forge.feature.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.development.forge.core.components.ForgeSectionLabel
import com.development.forge.core.components.ForgeTaskRow
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.domain.model.Task

@Preview
@Composable
fun TaskListScreen(
    modifier: Modifier = Modifier,
    tasks: List<Task> = com.development.forge.domain.model.tasks,
    onToggle: (String) -> Unit = { },
    onOpen: (String) -> Unit = { },
    onBack: () -> Unit = { },
    onAdd: () -> Unit = { }
) {
    val active = tasks.filter { !it.done }
    val done = tasks.filter { it.done }

    // mutually exclusive buckets — every active task lands in exactly one
    val today = active.filter { it.overdue || (it.due?.startsWith("Today") == true) }
    val upcoming = active.filter {
        !it.overdue && it.due != null && !it.due.startsWith("Today")
    }
    val someday = active.filter { it.due == null }

    val todayLabel = "TODAY · ${today.size}"
    val upcomingLabel = "UPCOMING · ${upcoming.size}"
    val somedayLabel = "SOMEDAY · ${someday.size}"
    val doneLabel = "DONE · ${done.size}"

    Column(modifier) {
        // ── Top bar ───────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .offset(x = (-10).dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFF5EDDE)
                    )
                }
                Text(
                    text = "All tasks",
                    style = TextStyle(
                        fontFamily = ForgeFonts.spaceGrotesk,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.W600,
                        letterSpacing = (-0.005).em,
                        color = Color(0xFFF5EDDE)
                    )
                )
            }
            IconButton(
                onClick = onAdd,
                modifier = Modifier
                    .size(44.dp)
                    .offset(x = 10.dp)   // push toward the right edge, not left
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFFF5EDDE)
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // ── The list ──────────────────────────────────────────
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 28.dp)
        ) {
            if (today.isNotEmpty()) {
                item(key = "today_header") {
                    ForgeSectionLabel(label = todayLabel)
                }
                items(today, key = { it.id }) { task ->
                    ForgeTaskRow(
                        task = task,
                        compact = false,
                        onToggle = { onToggle(task.id) },
                        onOpen = { onOpen(task.id) }
                    )
                }
            }

            if (upcoming.isNotEmpty()) {
                item(key = "upcoming_header") {
                    Spacer(Modifier.height(18.dp))
                    ForgeSectionLabel(label = upcomingLabel)
                }
                items(upcoming, key = { it.id }) { task ->
                    ForgeTaskRow(
                        task = task,
                        compact = false,
                        onToggle = { onToggle(task.id) },
                        onOpen = { onOpen(task.id) }
                    )
                }
            }

            if (someday.isNotEmpty()) {
                item(key = "someday_header") {
                    Spacer(Modifier.height(18.dp))
                    ForgeSectionLabel(label = somedayLabel)
                }
                items(someday, key = { it.id }) { task ->
                    ForgeTaskRow(
                        task = task,
                        compact = false,
                        onToggle = { onToggle(task.id) },
                        onOpen = { onOpen(task.id) }
                    )
                }
            }

            if (done.isNotEmpty()) {
                item(key = "done_header") {
                    Spacer(Modifier.height(24.dp))
                    ForgeSectionLabel(label = doneLabel)
                }
                items(done, key = { it.id }) { task ->
                    ForgeTaskRow(
                        task = task,
                        compact = false,
                        onToggle = { onToggle(task.id) },
                        onOpen = { onOpen(task.id) }
                    )
                }
            }
        }
    }
}