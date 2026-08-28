package com.development.forge.feature.tasks.viewmodel

import androidx.lifecycle.ViewModel
import com.development.forge.feature.tasks.contract.TaskEffect
import com.development.forge.feature.tasks.contract.TaskIntent
import com.development.forge.feature.tasks.contract.TaskState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor() : ViewModel() {

    private val _taskState = MutableStateFlow(TaskState())
    val taskState = _taskState.asStateFlow()

    private val _effect = Channel<TaskEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: TaskIntent) {

    }
}