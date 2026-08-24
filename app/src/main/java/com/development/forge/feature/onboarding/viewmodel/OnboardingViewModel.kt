package com.development.forge.feature.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.development.forge.core.datastore.ForgePreferences
import com.development.forge.feature.onboarding.contract.OnboardingEffect
import com.development.forge.feature.onboarding.contract.OnboardingIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferences: ForgePreferences
) : ViewModel() {

    private val _effect = Channel<OnboardingEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            OnboardingIntent.Finish -> {
                viewModelScope.launch {
                    preferences.setOnboardingDone(true)
                    _effect.send(OnboardingEffect.NavigateToRegister)
                }
            }
        }
    }
}