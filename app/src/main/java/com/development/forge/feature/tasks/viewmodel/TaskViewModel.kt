package com.development.forge.feature.tasks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.development.forge.data.repository.TaskRepository
import com.development.forge.domain.model.Priority
import com.development.forge.domain.model.Subtask
import com.development.forge.feature.tasks.contract.TaskEffect
import com.development.forge.feature.tasks.contract.TaskIntent
import com.development.forge.feature.tasks.contract.TaskState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Instant

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _taskState = MutableStateFlow(TaskState())
    val taskState = _taskState.asStateFlow()

    private val _effect = Channel<TaskEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            taskRepository.getVisibleTasks().collect { tasks ->
                _taskState.update { it.copy(tasks = tasks) }
            }
        }
    }

    fun onIntent(intent: TaskIntent) {
        when (intent) {
            is TaskIntent.StartEdit -> startEdit(intent.taskId)
            is TaskIntent.StartCreate -> startCreate()
            TaskIntent.ClearForm -> clearForm()
            is TaskIntent.UpdateTitle -> updateTitle(intent.title)
            is TaskIntent.UpdatePriority -> updatePriority(intent.priority)
            is TaskIntent.UpdateProject -> updateProject(intent.project)
            is TaskIntent.UpdateDueAt -> updateDueAt(intent.dueAt)
            is TaskIntent.ToggleDone -> toggleDone(intent.taskId)
            is TaskIntent.ToggleSubtask -> toggleSubtask(intent.subtaskId)
            is TaskIntent.UpdateNotes -> updateNotes(intent.notes)
            is TaskIntent.AddSubtask -> addSubTask(intent.title)
            is TaskIntent.RemoveSubtask -> removeSubTask(intent.index)
            is TaskIntent.SaveTask -> saveTask(intent.subtasks)
        }
    }

    private fun updateTitle(title: String) = _taskState.update { it.copy(title = title) }
    private fun updatePriority(priority: Priority) =
        _taskState.update { it.copy(priority = priority) }

    private fun updateProject(project: String?) = _taskState.update { it.copy(project = project) }
    private fun updateDueAt(dueAt: Instant?) = _taskState.update { it.copy(dueAt = dueAt) }
    private fun updateNotes(notes: String?) = _taskState.update { it.copy(notes = notes) }
    private fun clearForm() = _taskState.update { TaskState() }

    private fun addSubTask(title: String) =
        _taskState.update {
            it.copy(
                subtasks = it.subtasks + com.development.forge.domain.model.Subtask(
                    id = UUID.randomUUID().toString(),
                    title = title
                )
            )
        }

    private fun removeSubTask(index: Int) =
        _taskState.update {
            it.copy(subtasks = it.subtasks.toMutableList().apply { removeAt(index) })
        }

    private fun saveTask(subtasks: List<Subtask>) {
        val editingTaskId = _taskState.value.editingTaskId
        val title = _taskState.value.title
        if (title.isBlank()) return

        val priority = _taskState.value.priority
        val project = _taskState.value.project
        val dueAt = _taskState.value.dueAt
        val notes = _taskState.value.notes

        viewModelScope.launch {
            _taskState.update { it.copy(isSaving = true) }
            if (editingTaskId == null) {
                taskRepository.createTask(title, priority, dueAt, project, notes, subtasks)
            } else {
                taskRepository.updateTask(
                    editingTaskId,
                    title,
                    priority,
                    dueAt,
                    project,
                    notes,
                    subtasks
                )
            }
            _taskState.update { it.copy(isSaving = false) }
            _effect.send(TaskEffect.NavigateBack)
        }
    }

    private fun loadTask(taskId: String) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId) ?: return@launch
            _taskState.update {
                it.copy(
                    editingTaskId = taskId,
                    title = task.title,
                    priority = task.priority,
                    dueAt = task.dueAt,
                    project = task.project,
                    notes = task.notes,
                    subtasks = task.subtasks
                )
            }
        }
    }

    private fun startEdit(taskId: String) {
        loadTask(taskId)
        loadExistingProjects()
    }

    private fun startCreate() {
        clearForm()
        loadExistingProjects()
    }

    private fun toggleDone(taskId: String) {
        val task = _taskState.value.tasks.find { it.id == taskId } ?: return
        val newDone = !task.done
        viewModelScope.launch {
            if (newDone) {
                taskRepository.completeAllSubtasks(taskId)
            } else {
                taskRepository.toggleTaskDone(taskId, false)
            }
        }
    }

    private fun toggleSubtask(subtaskId: String) {
        val subtask = _taskState.value.tasks
            .flatMap { it.subtasks }
            .find { it.id == subtaskId } ?: return
        viewModelScope.launch {
            taskRepository.toggleSubtaskDone(subtaskId, !subtask.isDone)
        }
    }

    private fun loadExistingProjects() {
        viewModelScope.launch {
            _taskState.update {
                it.copy(existingProjects = taskRepository.getTopProjects())
            }
        }
    }
}