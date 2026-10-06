package com.muhazri.jejak.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.muhazri.jejak.core.designsystem.JejakTheme
import com.muhazri.jejak.core.designsystem.ScreenLayout
import com.muhazri.jejak.features.home.domain.entities.ActivityType
import com.muhazri.jejak.features.session.domain.entities.GPSSignal
import com.muhazri.jejak.features.session.domain.entities.SessionPhase
import com.muhazri.jejak.features.session.presentation.screens.ActiveSessionScreen
import com.muhazri.jejak.features.session.presentation.screens.ActiveSessionUiState
import com.muhazri.jejak.features.session.presentation.screens.SessionDetailScreen
import com.muhazri.jejak.features.session.presentation.screens.SessionReport
import com.muhazri.jejak.features.session.presentation.screens.SessionSummaryScreen
import com.muhazri.jejak.features.settings.domain.entities.DistanceUnit

/** A session a few minutes in, with a route recorded. */
private fun recordingState(
    phase: SessionPhase = SessionPhase.Recording,
    signal: GPSSignal = GPSSignal.Good,
    isLocked: Boolean = false,
) = ActiveSessionUiState(
    activity = ActivityType.Run,
    phase = phase,
    signal = signal,
    isLocked = isLocked,
    distanceMeters = 5_240.0,
    durationSeconds = 28 * 60.0 + 41,
    pausedForSeconds = 42.0,
    currentPace = 0.328,
    averagePace = 0.329,
    route = SampleData.route,
)

private val report = SessionReport(
    activity = ActivityType.Run,
    startDate = SampleData.session.startDate,
    endDate = SampleData.session.endDate,
    distanceMeters = SampleData.session.distanceMeters,
    durationSeconds = SampleData.session.durationSeconds,
    averagePace = SampleData.session.pace,
    route = SampleData.route,
    unit = DistanceUnit.Kilometers,
)

@Preview(name = "Recording", showBackground = true)
@Composable
private fun ActiveRecordingPreview() = JejakTheme {
    ActiveSessionScreen(ActivityType.Run, onClose = {}, state = recordingState())
}

@Preview(name = "Searching", showBackground = true)
@Composable
private fun ActiveSearchingPreview() = JejakTheme {
    ActiveSessionScreen(
        activity = ActivityType.Walk,
        onClose = {},
        state = ActiveSessionUiState(ActivityType.Walk),
    )
}

@Preview(name = "Paused", showBackground = true)
@Composable
private fun ActivePausedPreview() = JejakTheme {
    ActiveSessionScreen(
        activity = ActivityType.Run,
        onClose = {},
        state = recordingState(phase = SessionPhase.Paused),
    )
}

@Preview(name = "Locked", showBackground = true)
@Composable
private fun ActiveLockedPreview() = JejakTheme {
    ActiveSessionScreen(ActivityType.Run, onClose = {}, state = recordingState(isLocked = true))
}

@Preview(name = "Weak signal · dark", showBackground = true)
@Composable
private fun ActiveWeakSignalPreview() = JejakTheme(darkTheme = true) {
    ActiveSessionScreen(
        activity = ActivityType.Run,
        onClose = {},
        state = recordingState(signal = GPSSignal.Weak),
    )
}

@Preview(name = "Too short", showBackground = true)
@Composable
private fun ActiveTooShortPreview() = JejakTheme {
    ActiveSessionScreen(
        activity = ActivityType.Run,
        onClose = {},
        state = recordingState().copy(isShowingTooShort = true),
    )
}

@Preview(name = "Fold closed", widthDp = 400, heightDp = 566, showBackground = true)
@Composable
private fun ActiveFoldClosedPreview() = JejakTheme {
    ActiveSessionScreen(ActivityType.Run, onClose = {}, state = recordingState())
}

@Preview(name = "Fold open", widthDp = 800, heightDp = 566, showBackground = true)
@Composable
private fun ActiveFoldOpenPreview() = JejakTheme {
    ActiveSessionScreen(ActivityType.Run, onClose = {}, state = recordingState())
}

@Preview(name = "Summary", showBackground = true)
@Composable
private fun SummaryPreview() = JejakTheme {
    SessionSummaryScreen(report, ScreenLayout.Regular, onSave = {}, onDiscard = {})
}

@Preview(name = "Summary · fold open", widthDp = 800, heightDp = 566, showBackground = true)
@Composable
private fun SummaryFoldOpenPreview() = JejakTheme {
    SessionSummaryScreen(report, ScreenLayout.Split, onSave = {}, onDiscard = {})
}

@Preview(name = "Detail", showBackground = true)
@Composable
private fun DetailPreview() = JejakTheme {
    SessionDetailScreen(onBack = {}, session = SampleData.session)
}

@Preview(name = "Detail · dark", showBackground = true)
@Composable
private fun DetailDarkPreview() = JejakTheme(darkTheme = true) {
    SessionDetailScreen(onBack = {}, session = SampleData.session)
}

@Preview(name = "Detail · fold open", widthDp = 800, heightDp = 566, showBackground = true)
@Composable
private fun DetailFoldOpenPreview() = JejakTheme {
    SessionDetailScreen(onBack = {}, session = SampleData.session)
}
