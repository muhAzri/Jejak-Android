package com.muhazri.jejak.features.counter.domain.repositories

import com.muhazri.jejak.features.counter.domain.entities.Counter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet

class FakeCounterRepository : CounterRepository {

    private val state = MutableStateFlow(Counter())

    override fun observeCounter(): Flow<Counter> = state.asStateFlow()

    override suspend fun increment(): Counter = state.updateAndGet { Counter(it.value + 1) }

    override suspend fun reset() {
        state.value = Counter()
    }
}
