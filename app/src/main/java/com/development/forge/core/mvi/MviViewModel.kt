package com.development.forge.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base class for the unidirectional MVI loop shared by every feature ViewModel:
 *
 * ```
 * UI --onIntent(I)--> handleIntent --setState { }--> state: StateFlow<S> --> UI
 *                                  \--sendEffect(E)--> effects: Flow<E> (one-off)
 * ```
 *
 * @param S immutable UI state, @param I user intents, @param E one-off effects.
 */
abstract class MviViewModel<S : UiState, I : UiIntent, E : UiEffect> : ViewModel() {

    /** The state emitted before any intent is handled. Provided by each feature. */
    protected abstract val initialState: S

    // lazy so [initialState] is read after the subclass is fully constructed, not
    // during this base class's init (where the abstract val is not yet assigned).
    private val _state by lazy { MutableStateFlow(initialState) }

    /** The current, immutable UI state. Collected by the screen. */
    val state: StateFlow<S> by lazy { _state.asStateFlow() }

    // A buffered channel delivers each effect to a single collector and never
    // replays — so a rotation won't re-fire a navigation or toast.
    private val _effects = Channel<E>(Channel.BUFFERED)

    /** One-off effects (navigation, toasts). Not replayed to new collectors. */
    val effects: Flow<E> = _effects.receiveAsFlow()

    /** The latest state, for reducers that need to read before they write. */
    protected val currentState: S get() = _state.value

    /** The UI's single entry point for dispatching an intent. */
    fun onIntent(intent: I) = handleIntent(intent)

    /** Reduce an intent into state changes and/or effects. Implemented per feature. */
    protected abstract fun handleIntent(intent: I)

    /** Reducer helper: derive the next state from the current one. */
    protected fun setState(reducer: S.() -> S) = _state.update(reducer)

    /** Emit a one-off effect to the [effects] stream. */
    protected fun sendEffect(effect: E) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
