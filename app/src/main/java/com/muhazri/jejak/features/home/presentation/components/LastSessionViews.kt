package com.muhazri.jejak.features.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhazri.jejak.R
import com.muhazri.jejak.core.designsystem.HeroIcon
import com.muhazri.jejak.core.designsystem.HeroIconImage
import com.muhazri.jejak.core.designsystem.JejakFont
import com.muhazri.jejak.core.designsystem.JejakTheme
import com.muhazri.jejak.core.designsystem.ScreenLayout
import com.muhazri.jejak.features.home.domain.entities.SessionSummary
import com.muhazri.jejak.features.session.presentation.components.RouteColoring
import com.muhazri.jejak.features.session.presentation.components.StaticRouteMap
import com.muhazri.jejak.features.settings.domain.entities.DistanceUnit
import java.text.SimpleDateFormat
import java.util.Date

/** Shown under "Last Session" before anything has been saved. */
@Composable
fun EmptyLastSessionCard(fillsHeight: Boolean, modifier: Modifier = Modifier) {
    val colors = JejakTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        modifier = modifier
            .fillMaxWidth()
            .then(if (fillsHeight) Modifier.fillMaxHeight() else Modifier)
            .background(colors.surfaceTint, RoundedCornerShape(12.dp))
            .padding(horizontal = 24.dp, vertical = if (fillsHeight) 16.dp else 28.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(56.dp).background(colors.accent, CircleShape),
        ) {
            HeroIconImage(HeroIcon.Flag, 26.dp, colors.accentInk)
        }
        Text(
            text = stringResource(R.string.home_empty_title),
            style = JejakFont.h1,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.home_empty_message),
            style = JejakFont.p2,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

/** Summary row for the most recent session. */
@Composable
fun LastSessionRow(
    session: SessionSummary,
    unit: DistanceUnit,
    layout: ScreenLayout,
    modifier: Modifier = Modifier,
    onOpen: (() -> Unit)? = null,
) {
    val colors = JejakTheme.colors
    val distance = SessionFormat.distance(session.distanceMeters, unit)
    val details = "${SessionFormat.duration(session.durationSeconds)} · " +
        "${SessionFormat.pace(session.pace, unit)} /${unit.symbol}"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceTint, RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        // The open fold shows the route large above the row instead of a thumbnail.
        if (layout != ScreenLayout.Split) {
            val side = if (layout == ScreenLayout.Regular) 72.dp else 64.dp
            // The thumbnail is a map view, so it takes its own taps rather than the row's.
            RoutePreview(session, Modifier.size(side), onTap = onOpen)
        }

        Column(Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ActivityChip(session.activity, session.activity.tint.copy(alpha = 0.12f))
                Text(sessionDateLabel(session.startDate), style = JejakFont.p3, color = colors.textSecondary)
            }
            if (layout == ScreenLayout.Regular) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = distance,
                        style = JejakFont.display(20.sp),
                        color = colors.textPrimary,
                        modifier = Modifier.alignByBaseline(),
                    )
                    Text(
                        text = unit.symbol,
                        style = JejakFont.p2,
                        color = colors.textSecondary,
                        modifier = Modifier.alignByBaseline(),
                    )
                }
                Text(details, style = JejakFont.p2, color = colors.textSecondary)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "$distance ${unit.symbol}",
                        style = JejakFont.display(20.sp),
                        color = colors.textPrimary,
                        modifier = Modifier.alignByBaseline(),
                    )
                    Text(
                        text = details,
                        style = JejakFont.p2,
                        color = colors.textSecondary,
                        modifier = Modifier.alignByBaseline(),
                    )
                }
            }
        }

        HeroIconImage(HeroIcon.ChevronRight, 20.dp, colors.textPrimary)
    }
}

/** "Today · 06.12", "Yesterday · 6:12 AM", or the date for older sessions. */
@Composable
private fun sessionDateLabel(millis: Long): String {
    val locale = LocalResources.current.configuration.locales[0]
    val time = SimpleDateFormat.getTimeInstance(java.text.DateFormat.SHORT, locale).format(Date(millis))
    return when {
        SessionFormat.isToday(millis) -> stringResource(R.string.home_session_today, time)
        SessionFormat.isYesterday(millis) -> stringResource(R.string.home_session_yesterday, time)
        else -> {
            val day = SimpleDateFormat("d MMM", locale).format(Date(millis))
            stringResource(R.string.home_session_date, day, time)
        }
    }
}

/** The session's route on a small, static tile; a plain tile when it has no route. */
@Composable
fun RoutePreview(
    session: SessionSummary,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
    onTap: (() -> Unit)? = null,
) {
    val colors = JejakTheme.colors
    Box(
        modifier
            .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(colors.fillInput),
    ) {
        if (session.route.size > 1) {
            StaticRouteMap(
                route = session.route,
                coloring = RouteColoring.Solid(session.activity.tint),
                showsMarkers = false,
                lineWidth = 3.dp,
                onTap = onTap,
            )
        }
    }
}
