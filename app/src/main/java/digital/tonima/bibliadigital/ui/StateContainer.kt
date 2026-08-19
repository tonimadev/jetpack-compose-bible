package digital.tonima.bibliadigital.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface UiState

interface UiIntent

interface StateContainer<S : UiState> {
    val uiState: StateFlow<S>

    fun updateState(reducer: (S) -> S)
}

class StateContainerImpl<S : UiState>(
    initialState: S,
) : StateContainer<S> {
    private val _uiState = MutableStateFlow(initialState)
    override val uiState: StateFlow<S> = _uiState.asStateFlow()

    override fun updateState(reducer: (S) -> S) {
        _uiState.update { reducer(it) }
    }
}
