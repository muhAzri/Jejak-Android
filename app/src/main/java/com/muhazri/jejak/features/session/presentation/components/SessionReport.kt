package com.muhazri.jejak.features.session.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.muhazri.jejak.R
import com.muhazri.jejak.core.designsystem.JejakFont
import com.muhazri.jejak.core.designsystem.JejakTheme
import com.muhazri.jejak.core.map.OsmAttribution
import com.muhazri.jejak.features.home.domain.entities.ActivityType
import com.muhazri.jejak.features.home.domain.entities.RoutePoint
import com.muhazri.jejak.features.home.presentation.components.ActivityChip
import com.muhazri.jejak.features.home.presentation.components.SessionFormat
import com.muhazri.jejak.features.home.presentation.components.completedTitleRes
import com.muhazri.jejak.features.home.presentation.components.tint
import com.muhazri.jejak.features.session.domain.entities.RoutePace
import com.muhazri.jejak.features.settings.domain.entities.DistanceUnit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Activity chip, "Run Complete" and the start-end time; shared by the summary and a saved session's
 * detail.
 */
@Composable
fun SessionHeading(
    activity: ActivityType,
    startDate: Long?,
    endDate: Long?,
    titleSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    val colors = JejakTheme.colors
    Column(modifier) {
        ActivityChip(activity, activity.tint.copy(alpha = 0.12f))
        Text(
            text = stringResource(activity.completedTitleRes),
            style = JejakFont.display(titleSize),
            color = colors.textPrimary,
        )
        if (startDate != null && endDate != null) {
            val locale = Locale.getDefault()
            val day = SimpleDateFormat("EEE, MMM d", locale).format(Date(startDate))
            val time = SimpleDateFormat.getTimeInstance(java.text.DateFormat.SHORT, locale)
            Text(
                text = stringResource(
                    R.string.session_time_range,
                    day,
                    time.format(Date(startDate)),
                    time.format(Date(endDate)),
                ),
                style = JejakFont.p2,
                color = colors.textSecondary,
            )
        }
    }
}

/** The route colored by pace, with its legend; a solid line when pace can't be told apart. */
@Composable
fun SessionRouteMap(
    route: List<RoutePoint>,
    activity: ActivityType,
    unit: DistanceUnit,
    modifier: Modifier = Modifier,
) {
    val colors = JejakTheme.colors
    val extremes = RoutePace.extremes(route)
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.fillInput),
    ) {
        StaticRouteMap(
            route = route,
            coloring = if (extremes == null) RouteColoring.Solid(activity.tint) else RouteColoring.Pace,
            modifier = Modifier.fillMaxSize(),
        )
        OsmAttribution(Modifier.align(Alignment.BottomEnd).padding(8.dp))
        if (extremes != null) {
            PaceLegend(
                slowest = extremes.slowest,
                fastest = extremes.fastest,
                unit = unit,
                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
            )
        }
    }
}

/** "Slow 6:40 ▬ Fast 4:52" legend over the summary map. */
@Composable
fun PaceLegend(slowest: Double, fastest: Double, unit: DistanceUnit, modifier: Modifier = Modifier) {
    val colors = JejakTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = stringResource(R.string.pace_legend_slow, SessionFormat.pace(slowest, unit)),
            style = JejakFont.p3,
            color = colors.textPrimary,
        )
        Box(
            Modifier
                .width(48.dp)
                .height(6.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(colors.paceSlow, colors.accent, colors.paceFast),
                    ),
                    shape = CircleShape,
                ),
        )
        Text(
            text = stringResource(R.string.pace_legend_fast, SessionFormat.pace(fastest, unit)),
            style = JejakFont.p3,
            color = colors.textPrimary,
        )
    }
}

/** Distance, then time, average pace and best pace. */
@Composable
fun SessionMetrics(
    distanceMeters: Double,
    durationSeconds: Double,
    averagePace: Double?,
    route: List<RoutePoint>,
    unit: DistanceUnit,
    distanceSize: TextUnit,
    valueSize: TextUnit,
    spacing: Dp,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(spacing)) {
        DistanceMetric(distanceMeters, unit, distanceSize)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            MetricTile(
                title = stringResource(R.string.metric_time),
                value = SessionFormat.duration(durationSeconds),
                size = valueSize,
                modifier = Modifier.weight(1f),
            )
            MetricTile(
                title = stringResource(R.string.metric_avg_pace),
                value = SessionFormat.pace(averagePace, unit),
                size = valueSize,
                modifier = Modifier.weight(1f),
            )
            MetricTile(
                title = stringResource(R.string.metric_best_pace),
                value = SessionFormat.pace(RoutePace.extremes(route)?.fastest, unit),
                size = valueSize,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
