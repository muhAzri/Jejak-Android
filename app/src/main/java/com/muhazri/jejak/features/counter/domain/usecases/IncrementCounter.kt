package com.muhazri.jejak.features.counter.domain.usecases

import com.muhazri.jejak.features.counter.domain.entities.Counter
import com.muhazri.jejak.features.counter.domain.repositories.CounterRepository
import javax.inject.Inject

class IncrementCounter @Inject constructor(
    private val repository: CounterRepository,
) {
    suspend operator fun invoke(): Counter = repository.increment()
}
