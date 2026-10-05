package com.muhazri.jejak.features.counter.presentation.viewmodels

import com.muhazri.jejak.features.counter.domain.entities.Counter

data class CounterUiState(
    val counter: Counter = Counter(),
)

/** One-off effects that must not be replayed on recomposition or config change. */
sealed interface CounterEvent {
    data object CounterReset : CounterEvent
}
