package digital.tonima.bibliadigital.ui

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

interface UiState

interface UiIntent

interface UiEvent

interface StateContainer<S : UiState, E : UiEvent> {
    val uiState: StateFlow<S>
    val uiEvent: Flow<E>

    fun updateState(reducer: (S) -> S)

    fun sendEvent(event: E)
}

class StateContainerImpl<S : UiState, E : UiEvent>(
    initialState: S,
) : StateContainer<S, E> {
    private val _uiState = MutableStateFlow(initialState)
    override val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _uiEvent = Channel<E>(BUFFERED)
    override val uiEvent: Flow<E> = _uiEvent.receiveAsFlow()

    override fun updateState(reducer: (S) -> S) {
        _uiState.update { reducer(it) }
    }

    override fun sendEvent(event: E) {
        _uiEvent.trySend(event)
    }
}
