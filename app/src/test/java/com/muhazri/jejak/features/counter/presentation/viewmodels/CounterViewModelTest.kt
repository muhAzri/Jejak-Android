package com.muhazri.jejak.features.counter.presentation.viewmodels

import com.muhazri.jejak.features.counter.domain.repositories.FakeCounterRepository
import com.muhazri.jejak.features.counter.domain.usecases.IncrementCounter
import com.muhazri.jejak.features.counter.domain.usecases.ObserveCounter
import com.muhazri.jejak.features.counter.domain.usecases.ResetCounter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CounterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ui state mirrors the observed counter`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.onIncrementClicked()
        viewModel.onIncrementClicked()
        runCurrent()

        assertEquals(2, viewModel.uiState.value.counter.value)
    }

    @Test
    fun `reset clears the counter and emits an event`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.onIncrementClicked()
        runCurrent()
        viewModel.onResetClicked()
        runCurrent()

        assertEquals(0, viewModel.uiState.value.counter.value)
        assertEquals(CounterEvent.CounterReset, viewModel.events.first())
    }

    private fun createViewModel(
        repository: FakeCounterRepository = FakeCounterRepository(),
    ) = CounterViewModel(
        observeCounter = ObserveCounter(repository),
        incrementCounter = IncrementCounter(repository),
        resetCounter = ResetCounter(repository),
    )
}
