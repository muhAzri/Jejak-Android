package com.muhazri.jejak.features.session.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhazri.jejak.R
import com.muhazri.jejak.core.designsystem.ConfirmSheet
import com.muhazri.jejak.core.designsystem.DestructiveButton
import com.muhazri.jejak.core.designsystem.JejakTheme
import com.muhazri.jejak.core.designsystem.PrimaryButton
import com.muhazri.jejak.core.designsystem.ScreenLayout
import com.muhazri.jejak.core.designsystem.safeArea
import com.muhazri.jejak.features.home.domain.entities.ActivityType
import com.muhazri.jejak.features.home.domain.entities.RoutePoint
import com.muhazri.jejak.features.session.presentation.components.SessionHeading
import com.muhazri.jejak.features.session.presentation.components.SessionMetrics
import com.muhazri.jejak.features.session.presentation.components.SessionRouteMap
import com.muhazri.jejak.features.settings.domain.entities.DistanceUnit

/** What the finished session shows; the recording screen hands this over when the user stops. */
data class SessionReport(
    val activity: ActivityType,
    val startDate: Long,
    val endDate: Long,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val averagePace: Double?,
    val route: List<RoutePoint>,
    val unit: DistanceUnit,
)

/** Shown over the finished session; it can't be swiped away, the user has to save or discard. */
@Composable
fun SessionSummaryScreen(
    report: SessionReport,
    /** The presenting screen's layout: the sheet is smaller than the screen and would misread its shape. */
    layout: ScreenLayout,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = JejakTheme.colors
    val insets = safeArea()
    var isConfirmingDiscard by rememberSaveable { mutableStateOf(false) }

    Box(modifier.fillMaxSize().background(colors.surface)) {
        Box(Modifier.fillMaxSize().padding(top = insets.top, bottom = insets.bottom)) {
            when (layout) {
                ScreenLayout.Regular -> RegularSummary(report, onSave) { isConfirmingDiscard = true }
                ScreenLayout.Compact -> CompactSummary(report, onSave) { isConfirmingDiscard = true }
                ScreenLayout.Split -> SplitSummary(report, onSave, { isConfirmingDiscard = true }, insets.trailing)
            }
        }

        if (isConfirmingDiscard) {
            ConfirmSheet(
                title = stringResource(R.string.session_discard_title),
                message = stringResource(R.string.session_destroy_message),
                cancelTitle = stringResource(R.string.session_cancel),
                confirmTitle = stringResource(R.string.session_discard),
                onCancel = { isConfirmingDiscard = false },
                onConfirm = onDiscard,
                modifier = Modifier.padding(bottom = insets.bottom),
            )
        }
    }
}

@Composable
private fun RegularSummary(report: SessionReport, onSave: () -> Unit, onDiscard: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Heading(report, 32.sp, Modifier.padding(top = 24.dp, bottom = 12.dp))
            Map(report, Modifier.fillMaxWidth().height(220.dp))
            Metrics(report, 64.sp, 24.sp, 12.dp, Modifier.padding(top = 16.dp))
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp),
        ) {
            SaveButton(onSave)
            DiscardButton(onDiscard)
        }
    }
}

@Composable
private fun CompactSummary(report: SessionReport, onSave: () -> Unit, onDiscard: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Heading(report, 24.sp, Modifier.padding(top = 16.dp, bottom = 8.dp))
        Map(report, Modifier.fillMaxWidth().heightIn(min = 110.dp, max = 150.dp))
        Metrics(report, 48.sp, 20.sp, 8.dp, Modifier.padding(top = 12.dp))
        Spacer(Modifier.weight(1f).heightIn(min = 12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 4.dp),
        ) {
            DiscardButton(onDiscard, Modifier.weight(1f))
            SaveButton(onSave, Modifier.weight(1f))
        }
    }
}

/** Map on the left; title, metrics and decisions on the right, near the right thumb. */
@Composable
private fun SplitSummary(
    report: SessionReport,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    trailing: Dp,
) {
    Row(Modifier.fillMaxSize()) {
        Map(
            report,
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 16.dp, end = 24.dp)
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
            Heading(report, 32.sp)
            Metrics(report, 64.sp, 22.sp, 16.dp)
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DiscardButton(onDiscard, Modifier.weight(1f))
                SaveButton(onSave, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Heading(report: SessionReport, titleSize: TextUnit, modifier: Modifier = Modifier) {
    SessionHeading(report.activity, report.startDate, report.endDate, titleSize, modifier)
}

@Composable
private fun Map(report: SessionReport, modifier: Modifier) {
    SessionRouteMap(report.route, report.activity, report.unit, modifier)
}

@Composable
private fun Metrics(
    report: SessionReport,
    distanceSize: TextUnit,
    valueSize: TextUnit,
    spacing: Dp,
    modifier: Modifier = Modifier,
) {
    SessionMetrics(
        distanceMeters = report.distanceMeters,
        durationSeconds = report.durationSeconds,
        averagePace = report.averagePace,
        route = report.route,
        unit = report.unit,
        distanceSize = distanceSize,
        valueSize = valueSize,
        spacing = spacing,
        modifier = modifier,
    )
}

@Composable
private fun SaveButton(onSave: () -> Unit, modifier: Modifier = Modifier) {
    PrimaryButton(stringResource(R.string.session_save), onSave, modifier)
}

@Composable
private fun DiscardButton(onDiscard: () -> Unit, modifier: Modifier = Modifier) {
    DestructiveButton(stringResource(R.string.session_discard), onDiscard, modifier)
}
