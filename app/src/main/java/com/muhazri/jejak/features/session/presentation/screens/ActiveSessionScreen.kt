package com.muhazri.jejak.features.session.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhazri.jejak.R
import com.muhazri.jejak.core.designsystem.ConfirmSheet
import com.muhazri.jejak.core.designsystem.JejakFont
import com.muhazri.jejak.core.designsystem.JejakTheme
import com.muhazri.jejak.core.designsystem.ScreenLayout
import com.muhazri.jejak.core.designsystem.safeArea
import com.muhazri.jejak.features.home.domain.entities.ActivityType
import com.muhazri.jejak.features.home.domain.entities.RoutePoint
import com.muhazri.jejak.features.home.presentation.components.SessionFormat
import com.muhazri.jejak.features.home.presentation.components.tint
import com.muhazri.jejak.features.session.domain.entities.GPSSignal
import com.muhazri.jejak.features.session.domain.entities.SessionPhase
import com.muhazri.jejak.features.session.presentation.components.DistanceMetric
import com.muhazri.jejak.features.session.presentation.components.GPSSearchCard
import com.muhazri.jejak.features.session.presentation.components.HoldToUnlockBar
import com.muhazri.jejak.features.session.presentation.components.LiveSessionMap
import com.muhazri.jejak.features.session.presentation.components.MetricTile
import com.muhazri.jejak.features.session.presentation.components.PausedPill
import com.muhazri.jejak.features.session.presentation.components.SessionChips
import com.muhazri.jejak.features.session.presentation.components.SessionControls
import com.muhazri.jejak.features.settings.domain.entities.DistanceUnit
import androidx.compose.material3.Text

/** Everything the recording screen draws; the tracker fills this in once the location work lands. */
data class ActiveSessionUiState(
    val activity: ActivityType,
    val phase: SessionPhase = SessionPhase.Searching,
    val signal: GPSSignal = GPSSignal.Searching,
    val isLocked: Boolean = false,
    val isShowingTooShort: Boolean = false,
    val distanceMeters: Double = 0.0,
    val durationSeconds: Double = 0.0,
    val pausedForSeconds: Double = 0.0,
    val currentPace: Double? = null,
    val averagePace: Double? = null,
    val route: List<RoutePoint> = emptyList(),
    val unit: DistanceUnit = DistanceUnit.Kilometers,
)

/**
 * Full-screen recording view; follows the app's light or dark appearance. Folding or unfolding only
 * changes the layout: recording, pause and lock state live outside the view and carry over.
 */
@Composable
fun ActiveSessionScreen(
    activity: ActivityType,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    state: ActiveSessionUiState = ActiveSessionUiState(activity),
    onLock: () -> Unit = {},
    onUnlock: () -> Unit = {},
    onPauseResume: () -> Unit = {},
    onFinish: () -> Unit = {},
    onStartAnyway: () -> Unit = {},
    onKeepGoing: () -> Unit = {},
) {
    val colors = JejakTheme.colors
    val insets = safeArea()

    BoxWithConstraints(modifier.fillMaxSize().background(colors.canvas)) {
        val layout = ScreenLayout.of(DpSize(maxWidth, maxHeight))
        val half = maxWidth / 2
        val height = maxHeight

        val actions = SessionActions(onClose, onLock, onUnlock, onPauseResume, onFinish, onStartAnyway)
        if (layout == ScreenLayout.Split) {
            SplitSession(state, actions, half, insets.top, insets.trailing)
        } else {
            ColumnSession(state, actions, layout, insets.top, insets.leading, insets.trailing, insets.bottom, height)
        }

        if (state.isShowingTooShort) {
            ConfirmSheet(
                title = stringResource(R.string.session_too_short_title),
                message = stringResource(R.string.session_too_short_message),
                cancelTitle = stringResource(R.string.session_keep_going),
                confirmTitle = stringResource(R.string.session_discard),
                onCancel = onKeepGoing,
                onConfirm = onClose,
                modifier = Modifier.padding(bottom = insets.bottom),
            )
        }
    }
}

private class SessionActions(
    val onCancel: () -> Unit,
    val onLock: () -> Unit,
    val onUnlock: () -> Unit,
    val onPauseResume: () -> Unit,
    val onFinish: () -> Unit,
    val onStartAnyway: () -> Unit,
)

/** Phone and closed fold: map on top, metrics, then controls at the bottom. */
@Composable
private fun ColumnSession(
    state: ActiveSessionUiState,
    actions: SessionActions,
    layout: ScreenLayout,
    top: Dp,
    leading: Dp,
    trailing: Dp,
    bottom: Dp,
    screenHeight: Dp,
) {
    val isCompact = layout == ScreenLayout.Compact
    val mapHeight = if (isCompact) 200.dp else minOf(330.dp, screenHeight * 0.39f)

    Column(
        Modifier
            .fillMaxSize()
            .padding(start = leading, end = trailing, bottom = bottom),
    ) {
        MapArea(
            state = state,
            chipsTop = chipsTop(layout, top),
            fadesIntoCanvas = true,
            pillBottom = if (isCompact) 20.dp else 24.dp,
            modifier = Modifier.fillMaxWidth().height(mapHeight),
        )

        Metrics(
            state = state,
            distanceSize = if (isCompact) 64.sp else 88.sp,
            valueSize = if (isCompact) 22.sp else 24.sp,
            showsPaceNote = !isCompact,
            spacing = if (isCompact) 12.dp else 16.dp,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = if (isCompact) 12.dp else 8.dp),
        )

        Spacer(Modifier.weight(1f).heightIn(min = 12.dp))

        val horizontal = when {
            state.phase == SessionPhase.Searching -> if (isCompact) 24.dp else 16.dp
            state.isLocked -> 24.dp
            else -> if (isCompact) 24.dp else 32.dp
        }
        BottomArea(
            state = state,
            actions = actions,
            isCompact = isCompact,
            modifier = Modifier.padding(horizontal = horizontal).padding(bottom = 8.dp),
        )
    }
}

/** Fold open: the map fills the left panel; metrics and controls sit in the right one. */
@Composable
private fun SplitSession(
    state: ActiveSessionUiState,
    actions: SessionActions,
    half: Dp,
    top: Dp,
    trailing: Dp,
) {
    val colors = JejakTheme.colors
    Row(Modifier.fillMaxSize()) {
        MapArea(
            state = state,
            chipsTop = chipsTop(ScreenLayout.Split, top),
            fadesIntoCanvas = false,
            pillBottom = 20.dp,
            modifier = Modifier.width(half).fillMaxHeight(),
        )
        // The hinge.
        Box(Modifier.width(2.dp).fillMaxHeight().background(colors.border))
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 32.dp, end = ScreenLayout.splitOuterMargin(trailing))
                .padding(top = ScreenLayout.Split.topMargin(top) + top + 8.dp, bottom = 16.dp),
        ) {
            Metrics(state, 88.sp, 24.sp, showsPaceNote = false, spacing = 16.dp)
            Spacer(Modifier.weight(1f))
            BottomArea(state, actions, isCompact = false)
        }
    }
}

/**
 * The map starts below the status bar; on a fold (status bar often hidden) the chips also keep clear
 * of the rounded corners.
 */
private fun chipsTop(layout: ScreenLayout, top: Dp): Dp =
    if (layout == ScreenLayout.Regular) top + 12.dp else maxOf(12.dp, 24.dp - top) + top

@Composable
private fun MapArea(
    state: ActiveSessionUiState,
    chipsTop: Dp,
    fadesIntoCanvas: Boolean,
    pillBottom: Dp,
    modifier: Modifier = Modifier,
) {
    val colors = JejakTheme.colors
    Box(modifier.clipToBounds()) {
        LiveSessionMap(
            route = state.route,
            signal = state.signal,
            phase = state.phase,
            tint = state.activity.tint,
            isLocked = state.isLocked,
            modifier = Modifier.fillMaxSize(),
        )

        if (state.phase == SessionPhase.Searching) {
            Box(Modifier.fillMaxSize().background(colors.canvas.copy(alpha = 0.5f)))
        }
        if (fadesIntoCanvas) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, colors.canvas)),
                    ),
            )
        }
        SessionChips(
            activity = state.activity,
            signal = state.signal,
            phase = state.phase,
            isLocked = state.isLocked,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = chipsTop, start = 16.dp, end = 16.dp),
        )
        if (state.phase == SessionPhase.Paused) {
            PausedPill(
                pausedForSeconds = state.pausedForSeconds,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = pillBottom),
            )
        }
    }
}

@Composable
private fun Metrics(
    state: ActiveSessionUiState,
    distanceSize: TextUnit,
    valueSize: TextUnit,
    showsPaceNote: Boolean,
    spacing: Dp,
    modifier: Modifier = Modifier,
) {
    val colors = JejakTheme.colors
    if (state.phase == SessionPhase.Searching) {
        DistanceMetric(
            meters = 0.0,
            unit = state.unit,
            size = distanceSize,
            modifier = modifier.fillMaxWidth().alpha(0.4f),
        )
        return
    }

    val isDimmed = state.phase == SessionPhase.Paused
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacing)) {
        DistanceMetric(state.distanceMeters, state.unit, distanceSize, isDimmed = isDimmed)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            MetricTile(
                title = stringResource(R.string.metric_time),
                value = SessionFormat.duration(state.durationSeconds),
                size = valueSize,
                isDimmed = isDimmed,
                modifier = Modifier.weight(1f),
            )
            MetricTile(
                title = stringResource(R.string.metric_current_pace),
                value = SessionFormat.pace(state.currentPace, state.unit),
                size = valueSize,
                isDimmed = isDimmed || state.signal == GPSSignal.Weak,
                modifier = Modifier.weight(1f),
            )
            MetricTile(
                title = stringResource(R.string.metric_avg_pace),
                value = SessionFormat.pace(state.averagePace, state.unit),
                size = valueSize,
                isDimmed = isDimmed,
                modifier = Modifier.weight(1f),
            )
        }
        if (state.signal == GPSSignal.Weak && state.phase == SessionPhase.Recording) {
            Text(
                text = stringResource(R.string.metric_weak_signal_note),
                style = JejakFont.p3,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = (-4).dp),
            )
        } else if (showsPaceNote && !isDimmed) {
            Text(
                text = stringResource(R.string.metric_pace_note, state.unit.symbol),
                style = JejakFont.p3,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = (-10).dp),
            )
        }
    }
}

@Composable
private fun BottomArea(
    state: ActiveSessionUiState,
    actions: SessionActions,
    isCompact: Boolean,
    modifier: Modifier = Modifier,
) {
    when {
        state.phase == SessionPhase.Searching ->
            GPSSearchCard(actions.onCancel, actions.onStartAnyway, modifier)

        state.isLocked -> HoldToUnlockBar(isCompact, actions.onUnlock, modifier)

        else -> SessionControls(
            isPaused = state.phase == SessionPhase.Paused,
            isCompact = isCompact,
            onLock = actions.onLock,
            onPauseResume = actions.onPauseResume,
            onFinish = actions.onFinish,
            modifier = modifier,
        )
    }
}
