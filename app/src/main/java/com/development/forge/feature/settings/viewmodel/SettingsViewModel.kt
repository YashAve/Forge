package com.development.forge.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.development.forge.core.datastore.ForgePreferences
import com.development.forge.feature.settings.contract.SettingsEffect
import com.development.forge.feature.settings.contract.SettingsIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: ForgePreferences
) : ViewModel() {

    private val _effect = Channel<SettingsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.Logout -> logout()
            SettingsIntent.ToggleNotifications -> { }
            SettingsIntent.ToggleSound -> { }
        }
    }

    private fun logout() = viewModelScope.launch {
        preferences.deleteAuth()
        _effect.send(SettingsEffect.NavigateToLogin)
    }
}