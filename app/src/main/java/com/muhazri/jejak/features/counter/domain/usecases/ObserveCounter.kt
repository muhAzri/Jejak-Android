package com.muhazri.jejak.features.counter.domain.usecases

import com.muhazri.jejak.features.counter.domain.entities.Counter
import com.muhazri.jejak.features.counter.domain.repositories.CounterRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveCounter @Inject constructor(
    private val repository: CounterRepository,
) {
    operator fun invoke(): Flow<Counter> = repository.observeCounter()
}
