package com.muhazri.jejak.features.session.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muhazri.jejak.features.session.presentation.viewmodels.SessionDetailViewModel

/** Wires [SessionDetailScreen] to the saved session behind [sessionId]. */
@Composable
fun SessionDetailRoute(
    sessionId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SessionDetailViewModel = hiltViewModel<SessionDetailViewModel, SessionDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(sessionId) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Deleting leaves nothing to show, and a session that is already gone can't be opened.
    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onBack()
    }

    state.session?.let { session ->
        SessionDetailScreen(
            onBack = onBack,
            modifier = modifier,
            session = session,
            unit = state.unit,
            onDelete = viewModel::delete,
        )
    }
}
