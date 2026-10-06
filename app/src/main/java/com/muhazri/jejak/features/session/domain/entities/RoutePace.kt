package com.muhazri.jejak.features.session.domain.entities

import com.muhazri.jejak.features.home.domain.entities.RoutePoint
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Great-circle distance between fixes, in meters. */
object Geo {
    private const val EARTH_RADIUS = 6_371_000.0

    fun distance(a: RoutePoint, b: RoutePoint): Double =
        distance(a.latitude, a.longitude, b.latitude, b.longitude)

    fun distance(lat1Degrees: Double, lon1Degrees: Double, lat2Degrees: Double, lon2Degrees: Double): Double {
        val lat1 = Math.toRadians(lat1Degrees)
        val lat2 = Math.toRadians(lat2Degrees)
        val dLat = lat2 - lat1
        val dLon = Math.toRadians(lon2Degrees - lon1Degrees)
        val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        return 2 * EARTH_RADIUS * asin(sqrt(h.coerceIn(0.0, 1.0)))
    }
}

/** Fastest and slowest stretch of a route, in seconds per meter. */
data class PaceExtremes(val fastest: Double, val slowest: Double)

/** Pace along a route, in seconds per meter. Only measured within a segment, so pauses never count. */
object RoutePace {

    /** Current pace: the last [windowSeconds] of the latest segment. Null while standing still. */
    fun recent(route: List<RoutePoint>, windowSeconds: Double = 30.0, minDistance: Double = 15.0): Double? {
        val last = route.lastOrNull() ?: return null
        var distance = 0.0
        var first = last
        for (index in route.lastIndex - 1 downTo 0) {
            val point = route[index]
            if (point.segment != last.segment) break
            if ((last.timestamp - point.timestamp) / 1_000.0 > windowSeconds) break
            distance += Geo.distance(point, first)
            first = point
        }
        val elapsed = (last.timestamp - first.timestamp) / 1_000.0
        return if (distance >= minDistance && elapsed > 0) elapsed / distance else null
    }

    /** Pace at each point, over the stretch of [span] meters ending there. Null until a segment covers [span]. */
    fun perPoint(route: List<RoutePoint>, span: Double = 200.0): List<Double?> {
        if (route.isEmpty()) return emptyList()

        // Distance from the segment start to each point.
        val cumulative = DoubleArray(route.size)
        for (index in 1 until route.size) {
            val sameSegment = route[index].segment == route[index - 1].segment
            cumulative[index] =
                if (sameSegment) cumulative[index - 1] + Geo.distance(route[index - 1], route[index]) else 0.0
        }

        val paces = ArrayList<Double?>(route.size)
        var start = 0
        for (end in route.indices) {
            if (route[end].segment != route[start].segment) start = end
            // Move the window start forward while it still spans at least `span`.
            while (start < end && cumulative[end] - cumulative[start + 1] >= span) start++
            val distance = cumulative[end] - cumulative[start]
            val elapsed = (route[end].timestamp - route[start].timestamp) / 1_000.0
            paces.add(if (distance >= span && elapsed > 0) elapsed / distance else null)
        }
        return paces
    }

    /** Fastest and slowest stretch of the route; null when no stretch was long enough. */
    fun extremes(route: List<RoutePoint>, span: Double = 200.0): PaceExtremes? {
        val paces = perPoint(route, span).filterNotNull()
        val fastest = paces.minOrNull() ?: return null
        val slowest = paces.maxOrNull() ?: return null
        return PaceExtremes(fastest, slowest)
    }
}
