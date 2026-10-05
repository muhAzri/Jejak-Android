package com.muhazri.jejak.features.counter.data.datasources

import com.muhazri.jejak.core.di.IoDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext

/**
 * In-memory placeholder store. Swap the backing [MutableStateFlow] for DataStore or Room
 * once the counter has to survive process death.
 */
@Singleton
class CounterLocalDataSource @Inject constructor(
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    private val state = MutableStateFlow(INITIAL_VALUE)

    val value: Flow<Int> = state.asStateFlow()

    suspend fun increment(): Int = withContext(ioDispatcher) {
        state.updateAndGet { current -> current + 1 }
    }

    suspend fun reset() = withContext(ioDispatcher) {
        state.value = INITIAL_VALUE
    }

    private companion object {
        const val INITIAL_VALUE = 0
    }
}
