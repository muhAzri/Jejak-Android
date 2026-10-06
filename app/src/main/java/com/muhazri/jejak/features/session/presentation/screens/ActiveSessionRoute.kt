package com.muhazri.jejak.features.session.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
    SessionHaptics(state.phase, state.isLocked)

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

/**
 * The taps that mark a session's turning points, matching the iOS build: a confirmation when the
 * session ends, a toggle when pausing or resuming, and a light tick when the screen locks.
 *
 * Nothing fires on the first composition — only on an actual change — and resuming from the initial
 * GPS search is the start of recording, not a resume, so it stays silent.
 */
@Composable
private fun SessionHaptics(phase: SessionPhase, isLocked: Boolean) {
    val haptics = LocalHapticFeedback.current
    var lastPhase by remember { mutableStateOf(phase) }
    var lastLock by remember { mutableStateOf(isLocked) }

    LaunchedEffect(phase) {
        val previous = lastPhase
        lastPhase = phase
        when {
            phase == previous -> Unit
            phase == SessionPhase.Finished -> haptics.performHapticFeedback(HapticFeedbackType.Confirm)
            phase == SessionPhase.Paused -> haptics.performHapticFeedback(HapticFeedbackType.ToggleOff)
            phase == SessionPhase.Recording && previous != SessionPhase.Searching ->
                haptics.performHapticFeedback(HapticFeedbackType.ToggleOn)

            else -> Unit
        }
    }

    LaunchedEffect(isLocked) {
        val previous = lastLock
        lastLock = isLocked
        if (isLocked != previous) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
    }
}
