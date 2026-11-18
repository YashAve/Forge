package com.development.forge.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.development.forge.core.datastore.ForgePreferences
import com.development.forge.feature.settings.contract.SettingsEffect
import com.development.forge.feature.settings.contract.SettingsIntent
import com.development.forge.feature.settings.contract.SettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: ForgePreferences
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())

    val state = combine(
        preferences.userEmail(),
        _state
    ) { email, local ->
        SettingsState(
            email = email
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsState(email = "")
    )

    private val _effect = Channel<SettingsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.Logout -> logout()
            SettingsIntent.ToggleNotifications -> { }
            SettingsIntent.ToggleSound -> { }
            SettingsIntent.ToggleTheme -> { }
        }
    }

    private fun logout() = viewModelScope.launch {
        preferences.deleteAuth()
        _effect.send(SettingsEffect.NavigateToLogin)
    }
}