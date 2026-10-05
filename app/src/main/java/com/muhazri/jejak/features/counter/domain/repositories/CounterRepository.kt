package com.muhazri.jejak.features.counter.domain.repositories

import com.muhazri.jejak.features.counter.domain.entities.Counter
import kotlinx.coroutines.flow.Flow

interface CounterRepository {

    fun observeCounter(): Flow<Counter>

    suspend fun increment(): Counter

    suspend fun reset()
}
