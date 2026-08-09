package digital.tonima.bibliadigital.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import digital.tonima.bibliadigital.domain.core.exception.Failure
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface UiState

interface UiIntent

interface UiEvent

abstract class BaseViewModel<S : UiState, I : UiIntent, E : UiEvent> : ViewModel() {
    private val initialState: S by lazy { createInitialState() }

    abstract fun createInitialState(): S

    private val _uiState = MutableStateFlow(initialState)
    val uiState = _uiState.asStateFlow()

    private val _intent = MutableSharedFlow<I>()
    val intent = _intent.asSharedFlow()

    private val _event = Channel<E>()
    val event = _event.receiveAsFlow()

    init {
        subscribeIntents()
    }

    private fun subscribeIntents() {
        viewModelScope.launch {
            intent.collect {
                handleIntent(it)
            }
        }
    }

    abstract fun handleIntent(intent: I)

    fun sendIntent(intent: I) {
        viewModelScope.launch {
            _intent.emit(intent)
        }
    }

    protected fun setState(reduce: S.() -> S) {
        _uiState.update { it.reduce() }
    }

    protected fun sendEvent(event: E) {
        viewModelScope.launch {
            _event.send(event)
        }
    }

    protected fun handleFailure(failure: Failure) {
        // This could be handled via UiEvent or a field in UiState
    }
}
