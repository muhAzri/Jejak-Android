package com.muhazri.jejak.features.session.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhazri.jejak.R
import com.muhazri.jejak.core.designsystem.ConfirmSheet
import com.muhazri.jejak.core.designsystem.DestructiveButton
import com.muhazri.jejak.core.designsystem.HeroIcon
import com.muhazri.jejak.core.designsystem.HeroIconImage
import com.muhazri.jejak.core.designsystem.JejakTheme
import com.muhazri.jejak.core.designsystem.ScreenLayout
import com.muhazri.jejak.core.designsystem.pressable
import com.muhazri.jejak.core.designsystem.safeArea
import com.muhazri.jejak.features.home.domain.entities.ActivityType
import com.muhazri.jejak.features.home.domain.entities.SessionSummary
import com.muhazri.jejak.features.session.presentation.components.SessionHeading
import com.muhazri.jejak.features.session.presentation.components.SessionMetrics
import com.muhazri.jejak.features.session.presentation.components.SessionRouteMap
import com.muhazri.jejak.features.settings.domain.entities.DistanceUnit

/** A saved session, opened from Home's "Last Session": the route, its metrics, and a way to delete it. */
@Composable
fun SessionDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    session: SessionSummary = SessionSummary(
        activity = ActivityType.Run,
        startDate = System.currentTimeMillis() - 3_600_000,
        distanceMeters = 5_240.0,
        durationSeconds = 28 * 60.0 + 41,
    ),
    unit: DistanceUnit = DistanceUnit.Kilometers,
    onDelete: () -> Unit = {},
) {
    val colors = JejakTheme.colors
    val insets = safeArea()
    var isConfirmingDelete by rememberSaveable { mutableStateOf(false) }

    BoxWithConstraints(modifier.fillMaxSize().background(colors.surface)) {
        val layout = ScreenLayout.of(DpSize(maxWidth, maxHeight))
        val half = maxWidth / 2
        Box(
            Modifier
                .fillMaxSize()
                .padding(top = insets.top + layout.topMargin(insets.top), bottom = insets.bottom),
        ) {
            when (layout) {
                ScreenLayout.Regular -> RegularDetail(
                    session,
                    unit,
                    onBack,
                    { isConfirmingDelete = true },
                    Modifier.padding(start = insets.leading, end = insets.trailing),
                )

                ScreenLayout.Compact -> CompactDetail(
                    session,
                    unit,
                    onBack,
                    { isConfirmingDelete = true },
                    Modifier.padding(start = insets.leading, end = insets.trailing),
                )

                ScreenLayout.Split -> SplitDetail(
                    session,
                    unit,
                    onBack,
                    { isConfirmingDelete = true },
                    half,
                    insets.leading,
                    insets.trailing,
                )
            }
        }

        if (isConfirmingDelete) {
            ConfirmSheet(
                title = stringResource(R.string.session_delete_title),
                message = stringResource(R.string.session_destroy_message),
                cancelTitle = stringResource(R.string.session_cancel),
                confirmTitle = stringResource(R.string.session_delete),
                onCancel = { isConfirmingDelete = false },
                onConfirm = {
                    onDelete()
                    onBack()
                },
                modifier = Modifier.padding(bottom = insets.bottom),
            )
        }
    }
}

@Composable
private fun RegularDetail(
    session: SessionSummary,
    unit: DistanceUnit,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            BackButton(44.dp, onBack, Modifier.padding(top = 6.dp))
            SessionHeading(
                activity = session.activity,
                startDate = session.startDate,
                endDate = session.endDate,
                titleSize = 32.sp,
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
            )
            SessionRouteMap(session.route, session.activity, unit, Modifier.fillMaxWidth().height(220.dp))
            Metrics(session, unit, 64.sp, 24.sp, 12.dp, Modifier.padding(top = 16.dp))
        }
        DeleteButton(onDelete, Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp))
    }
}

@Composable
private fun CompactDetail(
    session: SessionSummary,
    unit: DistanceUnit,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            BackButton(44.dp, onBack)
            SessionHeading(session.activity, session.startDate, session.endDate, 24.sp)
        }
        SessionRouteMap(
            route = session.route,
            activity = session.activity,
            unit = unit,
            modifier = Modifier.fillMaxWidth().weight(1f).heightIn(min = 110.dp),
        )
        Metrics(session, unit, 48.sp, 20.sp, 8.dp, Modifier.padding(top = 12.dp))
        DeleteButton(onDelete, Modifier.padding(top = 12.dp))
    }
}

/** Route on the left; back, title and metrics on the right, like the summary. */
@Composable
private fun SplitDetail(
    session: SessionSummary,
    unit: DistanceUnit,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    half: Dp,
    leading: Dp,
    trailing: Dp,
) {
    Row(Modifier.fillMaxSize()) {
        SessionRouteMap(
            route = session.route,
            activity = session.activity,
            unit = unit,
            modifier = Modifier
                .width(half)
                .fillMaxHeight()
                .padding(start = maxOf(16.dp, leading), end = 24.dp)
                .padding(top = 16.dp, bottom = 24.dp),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 24.dp, end = ScreenLayout.splitOuterMargin(trailing))
                .padding(top = 16.dp, bottom = 24.dp),
        ) {
            BackButton(52.dp, onBack)
            SessionHeading(session.activity, session.startDate, session.endDate, 32.sp)
            Metrics(session, unit, 64.sp, 22.sp, 16.dp)
            Spacer(Modifier.weight(1f))
            DeleteButton(onDelete)
        }
    }
}

@Composable
private fun Metrics(
    session: SessionSummary,
    unit: DistanceUnit,
    distanceSize: androidx.compose.ui.unit.TextUnit,
    valueSize: androidx.compose.ui.unit.TextUnit,
    spacing: Dp,
    modifier: Modifier = Modifier,
) {
    SessionMetrics(
        distanceMeters = session.distanceMeters,
        durationSeconds = session.durationSeconds,
        averagePace = session.pace,
        route = session.route,
        unit = unit,
        distanceSize = distanceSize,
        valueSize = valueSize,
        spacing = spacing,
        modifier = modifier,
    )
}

/** Same filled circle as Settings' back button. */
@Composable
internal fun BackButton(size: Dp, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = JejakTheme.colors
    val label = stringResource(R.string.settings_back)
    val interaction = remember { MutableInteractionSource() }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .background(colors.surfaceTint, CircleShape)
            .pressable(interaction, onClick = onBack)
            .semantics { contentDescription = label },
    ) {
        HeroIconImage(HeroIcon.ChevronLeft, if (size > 44.dp) 26.dp else 22.dp, colors.textPrimary)
    }
}

@Composable
private fun DeleteButton(onDelete: () -> Unit, modifier: Modifier = Modifier) {
    DestructiveButton(stringResource(R.string.session_delete_button), onDelete, modifier)
}
