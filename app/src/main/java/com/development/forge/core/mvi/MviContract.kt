package com.development.forge.core.mvi

/**
 * Marker contracts for the MVI (Model–View–Intent) loop. Every feature declares
 * its own concrete implementations so the [MviViewModel] type parameters stay
 * feature-specific and mistakes (e.g. sending a HomeIntent to a SplashViewModel)
 * are caught at compile time.
 */

/** An immutable snapshot of everything a screen needs to render. */
interface UiState

/** A user (or system) event dispatched from the UI into a ViewModel. */
interface UiIntent

/** A one-off side effect — navigation, toast, snackbar — consumed exactly once. */
interface UiEffect
