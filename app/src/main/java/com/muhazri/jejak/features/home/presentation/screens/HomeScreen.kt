package com.muhazri.jejak.features.home.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
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
import com.muhazri.jejak.core.designsystem.HeroIcon
import com.muhazri.jejak.core.designsystem.HeroIconImage
import com.muhazri.jejak.core.designsystem.JejakFont
import com.muhazri.jejak.core.designsystem.JejakTheme
import com.muhazri.jejak.core.designsystem.ScreenLayout
import com.muhazri.jejak.core.designsystem.pressable
import com.muhazri.jejak.core.designsystem.safeArea
import com.muhazri.jejak.features.home.domain.entities.ActivityType
import com.muhazri.jejak.features.home.domain.entities.SessionSummary
import com.muhazri.jejak.features.home.presentation.components.EmptyLastSessionCard
import com.muhazri.jejak.features.home.presentation.components.LastSessionRow
import com.muhazri.jejak.features.home.presentation.components.LocationAllowedOnceNotice
import com.muhazri.jejak.features.home.presentation.components.LocationDeniedNotice
import com.muhazri.jejak.features.home.presentation.components.LocationNotRequestedNotice
import com.muhazri.jejak.features.home.presentation.components.RoutePreview
import com.muhazri.jejak.features.home.presentation.components.StartActivityCard
import com.muhazri.jejak.features.onboarding.domain.entities.LocationPermission
import com.muhazri.jejak.features.settings.domain.entities.DistanceUnit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Location notice shown above the start cards. */
enum class HomeNotice {
    None,

    /** Never asked (e.g. "Not Now" in onboarding): offers the system dialog right here. */
    LocationNotRequested,
    LocationDenied,
    LocationAllowedOnce,
}

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onStartActivity: (ActivityType) -> Unit,
    onOpenLastSession: (SessionSummary) -> Unit,
    modifier: Modifier = Modifier,
    permission: LocationPermission = LocationPermission.WhenInUse,
    lastSession: SessionSummary? = null,
    unit: DistanceUnit = DistanceUnit.Kilometers,
    isRequestingPermission: Boolean = false,
    onOpenSystemSettings: () -> Unit = {},
    onRequestPermission: () -> Unit = {},
) {
    val colors = JejakTheme.colors
    val insets = safeArea()
    var isOnceNoticeDismissed by rememberSaveable { mutableStateOf(false) }

    val notice = when {
        permission == LocationPermission.NotDetermined -> HomeNotice.LocationNotRequested
        permission == LocationPermission.Denied -> HomeNotice.LocationDenied
        permission == LocationPermission.AllowedOnce && !isOnceNoticeDismissed -> HomeNotice.LocationAllowedOnce
        else -> HomeNotice.None
    }
    /**
     * With a denied or allowed-once notice, the single-column layouts stay focused on the fix and drop
     * "Last Session". The open fold has room for both, unless location is off.
     */
    val showsLastSession = notice == HomeNotice.None || notice == HomeNotice.LocationNotRequested
    // Only an outright denial locks the cards: tapping one otherwise brings up the system dialog.
    val canStart = permission != LocationPermission.Denied

    val state = HomeState(
        notice = notice,
        lastSession = lastSession,
        unit = unit,
        canStart = canStart,
        showsLastSession = showsLastSession,
        isRequestingPermission = isRequestingPermission,
        onStartActivity = onStartActivity,
        onOpenLastSession = onOpenLastSession,
        onOpenSettings = onOpenSettings,
        onOpenSystemSettings = onOpenSystemSettings,
        onRequestPermission = onRequestPermission,
        onDismissOnceNotice = { isOnceNoticeDismissed = true },
    )

    BoxWithConstraints(modifier.fillMaxSize().background(colors.surface)) {
        val layout = ScreenLayout.of(DpSize(maxWidth, maxHeight))
        val half = maxWidth / 2
        Box(
            Modifier
                .fillMaxSize()
                .padding(top = insets.top + layout.topMargin(insets.top), bottom = insets.bottom),
        ) {
            when (layout) {
                ScreenLayout.Regular -> RegularHome(
                    state,
                    Modifier.padding(start = insets.leading, end = insets.trailing),
                )

                ScreenLayout.Compact -> CompactHome(
                    state,
                    Modifier.padding(start = insets.leading, end = insets.trailing),
                )

                ScreenLayout.Split -> SplitHome(state, half, insets.leading, insets.trailing)
            }
        }
    }
}

/** Everything the layouts need; the screen's three shapes differ only in arrangement. */
private class HomeState(
    val notice: HomeNotice,
    val lastSession: SessionSummary?,
    val unit: DistanceUnit,
    val canStart: Boolean,
    val showsLastSession: Boolean,
    val isRequestingPermission: Boolean,
    val onStartActivity: (ActivityType) -> Unit,
    val onOpenLastSession: (SessionSummary) -> Unit,
    val onOpenSettings: () -> Unit,
    val onOpenSystemSettings: () -> Unit,
    val onRequestPermission: () -> Unit,
    val onDismissOnceNotice: () -> Unit,
)

/** Phone: one scrolling column. */
@Composable
private fun RegularHome(state: HomeState, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
    ) {
        Header(state.onOpenSettings, titleSize = 32, modifier = Modifier.padding(top = 4.dp, bottom = 20.dp))

        Notice(state, ScreenLayout.Regular, Modifier.padding(bottom = 12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StartCard(state, ActivityType.Run, ScreenLayout.Regular)
            StartCard(state, ActivityType.Walk, ScreenLayout.Regular)
        }

        if (state.showsLastSession) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 28.dp),
            ) {
                LastSessionTitle()
                LastSession(state, ScreenLayout.Regular)
            }
        }
    }
}

/** Fold closed: start tiles side by side; the last session takes the remaining height. */
@Composable
private fun CompactHome(state: HomeState, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
    ) {
        Header(state.onOpenSettings, titleSize = 28)
        Notice(state, ScreenLayout.Compact)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StartCard(state, ActivityType.Run, ScreenLayout.Compact, Modifier.weight(1f))
            StartCard(state, ActivityType.Walk, ScreenLayout.Compact, Modifier.weight(1f))
        }

        if (state.showsLastSession) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp).weight(1f),
            ) {
                LastSessionTitle()
                LastSession(state, ScreenLayout.Compact)
            }
        } else {
            Spacer(Modifier.weight(1f))
        }
    }
}

/**
 * Fold open: start actions on the left, last session or the location problem on the right.
 * Each panel is exactly half the screen; outer edges keep 32dp (or the inset), the fold side 24dp.
 */
@Composable
private fun SplitHome(state: HomeState, half: Dp, leading: Dp, trailing: Dp) {
    Row(Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .width(half)
                .fillMaxHeight()
                .padding(start = ScreenLayout.splitOuterMargin(leading), end = 24.dp)
                .padding(bottom = 16.dp),
        ) {
            Header(state.onOpenSettings, titleSize = 32)
            // Both cards stretch, so they split the height left under the header rather than the
            // first one swallowing it.
            StartCard(state, ActivityType.Run, ScreenLayout.Split, Modifier.weight(1f))
            StartCard(state, ActivityType.Walk, ScreenLayout.Split, Modifier.weight(1f))
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 24.dp, end = ScreenLayout.splitOuterMargin(trailing))
                .padding(top = 22.dp, bottom = 16.dp),
        ) {
            if (state.notice == HomeNotice.LocationDenied) {
                LocationDeniedNotice(ScreenLayout.Split, state.onOpenSystemSettings)
            } else {
                Notice(state, ScreenLayout.Split)
                LastSessionTitle()
                LastSession(state, ScreenLayout.Split)
            }
        }
    }
}

@Composable
private fun Header(onOpenSettings: () -> Unit, titleSize: Int, modifier: Modifier = Modifier) {
    val colors = JejakTheme.colors
    val settingsLabel = stringResource(R.string.home_settings)
    val interaction = remember { MutableInteractionSource() }
    val today = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()) }

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.weight(1f)) {
            Text(today, style = JejakFont.p2Semibold, color = colors.textSecondary)
            Text("Jejak", style = JejakFont.display(titleSize.sp), color = colors.textPrimary)
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .background(colors.surfaceTint, CircleShape)
                .pressable(interaction, onClick = onOpenSettings)
                .semantics { contentDescription = settingsLabel },
        ) {
            HeroIconImage(HeroIcon.Cog, 24.dp, colors.textPrimary)
        }
    }
}

@Composable
private fun Notice(state: HomeState, layout: ScreenLayout, modifier: Modifier = Modifier) {
    when (state.notice) {
        HomeNotice.None -> Unit
        HomeNotice.LocationNotRequested -> LocationNotRequestedNotice(
            isRequesting = state.isRequestingPermission,
            onAllow = state.onRequestPermission,
            modifier = modifier,
        )

        HomeNotice.LocationDenied ->
            LocationDeniedNotice(layout, state.onOpenSystemSettings, modifier)

        HomeNotice.LocationAllowedOnce ->
            LocationAllowedOnceNotice(state.onOpenSystemSettings, state.onDismissOnceNotice, modifier)
    }
}

@Composable
private fun StartCard(
    state: HomeState,
    activity: ActivityType,
    layout: ScreenLayout,
    modifier: Modifier = Modifier,
) {
    StartActivityCard(
        activity = activity,
        layout = layout,
        isLocked = !state.canStart,
        onClick = { state.onStartActivity(activity) },
        modifier = modifier,
    )
}

@Composable
private fun LastSessionTitle() {
    Text(
        text = stringResource(R.string.home_last_session),
        style = JejakFont.h2,
        color = JejakTheme.colors.textPrimary,
    )
}

@Composable
private fun ColumnScope.LastSession(state: HomeState, layout: ScreenLayout) {
    val session = state.lastSession
    if (session == null) {
        EmptyLastSessionCard(fillsHeight = layout != ScreenLayout.Regular)
        return
    }
    val open = { state.onOpenLastSession(session) }
    val interaction = remember { MutableInteractionSource() }
    if (layout == ScreenLayout.Split) {
        RoutePreview(
            session = session,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp)
                .weight(1f),
            cornerRadius = 12.dp,
            onTap = open,
        )
    }
    LastSessionRow(
        session = session,
        unit = state.unit,
        layout = layout,
        modifier = Modifier.pressable(interaction, onClick = open),
        onOpen = open,
    )
}
