package com.muhazri.jejak.features.counter.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhazri.jejak.features.counter.domain.usecases.IncrementCounter
import com.muhazri.jejak.features.counter.domain.usecases.ObserveCounter
import com.muhazri.jejak.features.counter.domain.usecases.ResetCounter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CounterViewModel @Inject constructor(
    observeCounter: ObserveCounter,
    private val incrementCounter: IncrementCounter,
    private val resetCounter: ResetCounter,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CounterUiState())
    val uiState: StateFlow<CounterUiState> = _uiState.asStateFlow()

    private val _events = Channel<CounterEvent>(Channel.BUFFERED)
    val events: Flow<CounterEvent> = _events.receiveAsFlow()

    init {
        observeCounter()
            .onEach { counter -> _uiState.update { state -> state.copy(counter = counter) } }
            .launchIn(viewModelScope)
    }

    fun onIncrementClicked() {
        viewModelScope.launch { incrementCounter() }
    }

    fun onResetClicked() {
        viewModelScope.launch {
            resetCounter()
            _events.send(CounterEvent.CounterReset)
        }
    }
}
