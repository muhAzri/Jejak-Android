package com.muhazri.jejak.session

import com.muhazri.jejak.features.home.domain.entities.RoutePoint
import com.muhazri.jejak.features.session.domain.entities.Geo
import com.muhazri.jejak.features.session.domain.entities.LocationSample
import com.muhazri.jejak.features.session.domain.entities.TrackFilter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sqrt

/** The instant every synthetic track starts from, in epoch milliseconds. */
const val START_MILLIS = 1_000_000_000L

/** ~11.1 m per 0.0001° of latitude. */
val metersPerStep: Double = Geo.distance(0.0, 0.0, 0.0001, 0.0)

fun at(seconds: Double): Long = START_MILLIS + (seconds * 1_000).toLong()

/** A fix [step] × 0.0001° north of the origin. */
fun sample(step: Int, seconds: Double, accuracy: Double = 5.0) = LocationSample(
    latitude = step * 0.0001,
    longitude = 0.0,
    horizontalAccuracy = accuracy,
    timestamp = at(seconds),
)

/** Deterministic Gaussian noise, so the noisy-GPS tests are repeatable. */
class Noise(seed: ULong) {
    private var state = seed

    fun gaussian(sigma: Double): Double {
        val u1 = maxOf(uniform(), Double.MIN_VALUE)
        val u2 = uniform()
        return sigma * sqrt(-2 * ln(u1)) * cos(2 * PI * u2)
    }

    private fun uniform(): Double {
        state = state * 6_364_136_223_846_793_005uL + 1_442_695_040_888_963_407uL
        return (state shr 11).toDouble() / (1L shl 53).toDouble()
    }
}

/** A fix [east]/[north] meters from the origin, blurred by [sigma] meters of GPS noise. */
fun noisyFix(
    east: Double,
    north: Double,
    seconds: Double,
    sigma: Double,
    noise: Noise,
    altitude: Double = 0.0,
    verticalAccuracy: Double = -1.0,
) = LocationSample(
    latitude = (north + noise.gaussian(sigma)) / TrackFilter.METERS_PER_DEGREE,
    longitude = (east + noise.gaussian(sigma)) / TrackFilter.METERS_PER_DEGREE,
    horizontalAccuracy = sigma * 1.5,
    timestamp = at(seconds),
    altitude = altitude,
    verticalAccuracy = verticalAccuracy,
)

/** A route from altitudes alone, for the elevation rules. */
fun altitudeRoute(altitudes: List<Double?>, segments: List<Int>? = null): List<RoutePoint> =
    altitudes.mapIndexed { index, altitude ->
        RoutePoint(
            latitude = 0.0,
            longitude = 0.0,
            timestamp = index * 1_000L,
            segment = segments?.get(index) ?: 0,
            altitude = altitude,
        )
    }

/** A straight route whose points are spaced one step apart, taking the given seconds each. */
fun pacedRoute(secondsPerStep: List<Double>, segment: Int = 0): List<RoutePoint> {
    var time = 0.0
    return secondsPerStep.mapIndexed { step, seconds ->
        time += seconds
        RoutePoint(
            latitude = step * 0.0001,
            longitude = 0.0,
            timestamp = at(time),
            segment = segment,
        )
    }
}
