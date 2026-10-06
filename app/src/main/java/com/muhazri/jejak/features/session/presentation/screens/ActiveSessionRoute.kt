package com.muhazri.jejak.features.session.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muhazri.jejak.core.designsystem.ScreenLayout
import com.muhazri.jejak.features.home.domain.entities.ActivityType
import com.muhazri.jejak.features.session.domain.entities.SessionPhase
import com.muhazri.jejak.features.session.presentation.viewmodels.ActiveSessionViewModel

/**
 * Wires [ActiveSessionScreen] to its view model and puts the summary over it once the session ends.
 *
 * The summary follows the whole screen's layout rather than its own, and like iOS it can't be
 * dismissed: the user has to save or discard.
 */
@Composable
fun ActiveSessionRoute(
    activity: ActivityType,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActiveSessionViewModel = hiltViewModel<ActiveSessionViewModel, ActiveSessionViewModel.Factory>(
        creationCallback = { factory -> factory.create(activity) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val report by viewModel.report.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.runClock() }

    val cancel = {
        viewModel.discard()
        onClose()
    }

    // Leaving while searching costs nothing; once recording, only the session's own controls end it.
    BackHandler(enabled = true) {
        if (report == null && state.phase == SessionPhase.Searching) cancel()
    }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val layout = ScreenLayout.of(DpSize(maxWidth, maxHeight))
        ActiveSessionScreen(
            activity = state.activity,
            onClose = cancel,
            state = state,
            onLock = viewModel::lock,
            onUnlock = viewModel::unlock,
            onPauseResume = viewModel::togglePause,
            onFinish = viewModel::finish,
            onStartAnyway = viewModel::startRecording,
            onKeepGoing = viewModel::keepGoing,
        )

        report?.let { finished ->
            SessionSummaryScreen(
                report = finished,
                layout = layout,
                onSave = {
                    viewModel.save()
                    onClose()
                },
                onDiscard = {
                    viewModel.discard()
                    onClose()
                },
            )
        }
    }
}

