package com.muhazri.jejak.features.counter.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muhazri.jejak.R
import com.muhazri.jejak.core.ui.theme.JejakTheme
import com.muhazri.jejak.features.counter.presentation.components.CounterValue
import com.muhazri.jejak.features.counter.presentation.viewmodels.CounterEvent
import com.muhazri.jejak.features.counter.presentation.viewmodels.CounterUiState
import com.muhazri.jejak.features.counter.presentation.viewmodels.CounterViewModel

@Composable
fun CounterScreen(
    modifier: Modifier = Modifier,
    viewModel: CounterViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resetMessage = stringResource(R.string.counter_reset_message)

    LaunchedEffect(viewModel, resetMessage) {
        viewModel.events.collect { event ->
            when (event) {
                CounterEvent.CounterReset -> snackbarHostState.showSnackbar(resetMessage)
            }
        }
    }

    CounterContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIncrementClick = viewModel::onIncrementClicked,
        onResetClick = viewModel::onResetClicked,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CounterContent(
    state: CounterUiState,
    snackbarHostState: SnackbarHostState,
    onIncrementClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(text = stringResource(R.string.counter_title)) }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CounterValue(value = state.counter.value)
            Button(onClick = onIncrementClick) {
                Text(text = stringResource(R.string.counter_increment))
            }
            OutlinedButton(onClick = onResetClick) {
                Text(text = stringResource(R.string.counter_reset))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CounterContentPreview() {
    JejakTheme {
        CounterContent(
            state = CounterUiState(),
            snackbarHostState = remember { SnackbarHostState() },
            onIncrementClick = {},
            onResetClick = {},
        )
    }
}
