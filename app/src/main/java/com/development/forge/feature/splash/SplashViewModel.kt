package com.development.forge.feature.splash

import com.development.forge.core.mvi.MviViewModel

/**
 * Splash ViewModel. Structure-only stub: it wires the feature into [MviViewModel]
 * with an initial state and an empty intent reducer. Logic arrives in a later phase.
 */
class SplashViewModel : MviViewModel<SplashState, SplashIntent, SplashEffect>() {

    override val initialState: SplashState = SplashState()

    override fun handleIntent(intent: SplashIntent) {
        // No intents yet — placeholder.
    }
}
