package com.muhazri.jejak.features.session.presentation.components

import android.graphics.DashPathEffect
import android.graphics.Paint
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.muhazri.jejak.R
import com.muhazri.jejak.core.map.OsmMapView
import com.muhazri.jejak.core.map.frame
import com.muhazri.jejak.features.home.domain.entities.RoutePoint
import com.muhazri.jejak.features.session.domain.entities.RoutePace
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/** One polyline of a route: a stretch with the same segment, color and dash. */
data class RouteStroke(
    val points: List<RoutePoint>,
    val color: Color,
    val isDashed: Boolean,
)

sealed interface RouteColoring {
    /** One color; weak-signal stretches dotted (live map). */
    data class Solid(val color: Color) : RouteColoring

    /** Slow -> fast on the gray-yellow-red scale (summary map). */
    data object Pace : RouteColoring
}

object RouteStrokes {

    fun make(route: List<RoutePoint>, coloring: RouteColoring): List<RouteStroke> {
        if (route.size < 2) return emptyList()

        // Style of the edge ending at each point; consecutive edges with the same style join into one stroke.
        val styles: List<Pair<Color, Boolean>> = when (coloring) {
            is RouteColoring.Solid -> route.map { coloring.color to it.isEstimated }
            RouteColoring.Pace -> {
                val middle = paceBuckets.size / 2
                val extremes = RoutePace.extremes(route)
                RoutePace.perPoint(route).map { pace ->
                    // The first stretch of each segment has no pace yet; it takes the middle of the scale.
                    if (pace == null || extremes == null || extremes.slowest <= extremes.fastest) {
                        paceBuckets[middle] to false
                    } else {
                        val speed = (extremes.slowest - pace) / (extremes.slowest - extremes.fastest)
                        val bucket = Math.round(speed * (paceBuckets.size - 1)).toInt()
                        paceBuckets[bucket.coerceIn(paceBuckets.indices)] to false
                    }
                }
            }
        }

        val strokes = mutableListOf<RouteStroke>()
        var points = mutableListOf<RoutePoint>()
        var current: Pair<Color, Boolean>? = null

        fun flush() {
            val style = current
            if (points.size > 1 && style != null) {
                strokes += RouteStroke(points.toList(), style.first, style.second)
            }
            points = mutableListOf()
            current = null
        }

        for (index in 1 until route.size) {
            val previous = route[index - 1]
            val point = route[index]
            if (previous.segment != point.segment) {
                flush()
                continue
            }
            val style = styles[index]
            if (current != style) {
                flush()
                points = mutableListOf(previous)
                current = style
            }
            points += point
        }
        flush()
        return strokes
    }

    /** Seven steps from slow gray through yellow to fast red. */
    val paceBuckets: List<Color> = (0 until 7).map { step ->
        val t = step / 6.0
        if (t < 0.5) mix(0x9A9A9A, 0xFFEE00, t * 2) else mix(0xFFEE00, 0xDB0826, (t - 0.5) * 2)
    }

    private fun mix(a: Int, b: Int, t: Double): Color {
        fun channel(hex: Int, shift: Int) = ((hex shr shift) and 0xFF) / 255f
        fun lerp(shift: Int) = channel(a, shift) + (channel(b, shift) - channel(a, shift)) * t.toFloat()
        return Color(lerp(16), lerp(8), lerp(0))
    }
}

/** osmdroid wants its own point type; the route keeps ours. */
fun RoutePoint.toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)

/**
 * The route with a white casing, as on the design's map overlays: one polyline pair per stroke so
 * colour and dash can change along the way.
 */
internal fun MapView.addRoute(strokes: List<RouteStroke>, lineWidth: Float) {
    fun line(stroke: RouteStroke, configure: Paint.() -> Unit) = Polyline().apply {
        setPoints(stroke.points.map(RoutePoint::toGeoPoint))
        infoWindow = null
        outlinePaint.apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            configure()
        }
    }

    // Every casing goes down before any coloured line, so strokes never cut into their neighbours.
    strokes.forEach { stroke ->
        overlays.add(
            line(stroke) {
                color = android.graphics.Color.WHITE
                strokeWidth = lineWidth + 4
            },
        )
    }
    strokes.forEach { stroke ->
        overlays.add(
            line(stroke) {
                color = stroke.color.toArgb()
                strokeWidth = if (stroke.isDashed) lineWidth - 1 else lineWidth
                if (stroke.isDashed) pathEffect = DashPathEffect(floatArrayOf(2f, 9f), 0f)
            },
        )
    }
}

/** A marker drawn at its own centre, so the glyph sits exactly on the fix. */
private fun MapView.addMarker(point: RoutePoint, @DrawableRes icon: Int) {
    overlays.add(
        Marker(this).apply {
            position = point.toGeoPoint()
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            setIcon(ContextCompat.getDrawable(context, icon))
            setInfoWindow(null)
        },
    )
}

/**
 * A finished route on OpenStreetMap, framed to fit and not interactive: the summary map and Home's
 * route preview.
 */
@Composable
fun StaticRouteMap(
    route: List<RoutePoint>,
    coloring: RouteColoring,
    modifier: Modifier = Modifier,
    showsMarkers: Boolean = true,
    lineWidth: Dp = 5.dp,
    onTap: (() -> Unit)? = null,
) {
    val strokes = RouteStrokes.make(route, coloring)
    val widthPx = with(LocalDensity.current) { lineWidth.toPx() }
    val markerPaddingPx = with(LocalDensity.current) { 10.dp.roundToPx() }
    val padding = if (showsMarkers) maxOf(widthPx.toInt() + 4, markerPaddingPx) else widthPx.toInt() + 4

    OsmMapView(modifier, onTap = onTap) { map ->
        map.overlays.clear()
        map.addRoute(strokes, widthPx)
        if (showsMarkers && route.isNotEmpty()) {
            map.addMarker(route.first(), R.drawable.route_start_marker)
            map.addMarker(route.last(), R.drawable.route_end_marker)
        }
        map.frame(route.map(RoutePoint::toGeoPoint), padding)
    }
}
