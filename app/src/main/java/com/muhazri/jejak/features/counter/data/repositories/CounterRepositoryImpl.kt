package com.muhazri.jejak.features.counter.data.repositories

import com.muhazri.jejak.features.counter.data.datasources.CounterLocalDataSource
import com.muhazri.jejak.features.counter.domain.entities.Counter
import com.muhazri.jejak.features.counter.domain.repositories.CounterRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CounterRepositoryImpl @Inject constructor(
    private val localDataSource: CounterLocalDataSource,
) : CounterRepository {

    override fun observeCounter(): Flow<Counter> = localDataSource.value.map(::Counter)

    override suspend fun increment(): Counter = Counter(localDataSource.increment())

    override suspend fun reset() = localDataSource.reset()
}
