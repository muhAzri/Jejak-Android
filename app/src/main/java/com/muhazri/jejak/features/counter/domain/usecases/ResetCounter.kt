package com.muhazri.jejak.features.counter.domain.usecases

import com.muhazri.jejak.features.counter.domain.repositories.CounterRepository
import javax.inject.Inject

class ResetCounter @Inject constructor(
    private val repository: CounterRepository,
) {
    suspend operator fun invoke() = repository.reset()
}
