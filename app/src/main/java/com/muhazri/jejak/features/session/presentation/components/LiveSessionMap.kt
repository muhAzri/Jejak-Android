package com.muhazri.jejak.features.session.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.muhazri.jejak.core.designsystem.JejakTheme
import com.muhazri.jejak.core.map.OsmAttribution
import com.muhazri.jejak.core.map.OsmMapView
import com.muhazri.jejak.features.home.domain.entities.RoutePoint
import com.muhazri.jejak.features.session.domain.entities.GPSSignal
import com.muhazri.jejak.features.session.domain.entities.SessionPhase

/** Street-level zoom the live map holds while it follows the runner. */
private const val FOLLOW_ZOOM = 17.0

/**
 * The live map: the route so far over OpenStreetMap, centred on the latest fix, with the current
 * position pinned to the middle of the view.
 */
@Composable
fun LiveSessionMap(
    route: List<RoutePoint>,
    signal: GPSSignal,
    phase: SessionPhase,
    tint: Color,
    modifier: Modifier = Modifier,
    isLocked: Boolean = false,
) {
    val colors = JejakTheme.colors
    val isPaused = phase == SessionPhase.Paused
    val lineWidth = with(LocalDensity.current) { 5.dp.toPx() }
    val strokes = RouteStrokes.make(
        route = route,
        coloring = RouteColoring.Solid(if (isPaused) colors.paceSlow else tint),
    )

    Box(modifier.background(colors.fillInput), contentAlignment = Alignment.Center) {
        // Before the first fix there is nowhere to centre on; showing tiles then would mean zoom 0,
        // i.e. the whole world. The plain map surface is what the design shows while searching.
        val last = route.lastOrNull()
        if (last != null) {
            OsmMapView(
                modifier = Modifier.fillMaxSize(),
                // Locking the session locks the map with it, as on iOS.
                interactive = !isLocked,
            ) { map ->
                map.overlays.clear()
                map.addRoute(strokes, lineWidth)
                map.controller.setZoom(FOLLOW_ZOOM)
                map.controller.setCenter(last.toGeoPoint())
            }
            OsmAttribution(Modifier.align(Alignment.BottomEnd).padding(8.dp))
        }

        // The dot marks "here", which is the centre of a map that follows you.
        if (!isPaused && phase != SessionPhase.Searching) {
            PositionDot(isWeak = signal != GPSSignal.Good)
        }
    }
}

/** Yellow dot with a soft halo; gray with a wide accuracy ring when the signal is weak. */
@Composable
private fun PositionDot(isWeak: Boolean) {
    val colors = JejakTheme.colors
    Canvas(Modifier.fillMaxSize()) {
        if (isWeak) {
            drawCircle(colors.accent.copy(alpha = 0.12f), radius = 34.dp.toPx(), center = center)
            drawCircle(
                color = colors.accent.copy(alpha = 0.5f),
                radius = 34.dp.toPx(),
                center = center,
                style = Stroke(1.dp.toPx()),
            )
        } else {
            drawCircle(colors.accent.copy(alpha = 0.25f), radius = 21.dp.toPx(), center = center)
        }
        drawCircle(Color(0xFF262626), radius = 14.dp.toPx(), center = center)
        drawCircle(
            color = if (isWeak) colors.paceSlow else colors.accent,
            radius = 11.dp.toPx(),
            center = center,
        )
    }
}
