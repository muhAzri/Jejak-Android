package com.muhazri.jejak.features.session.domain.entities

import com.muhazri.jejak.features.home.domain.entities.RoutePoint

/** Smooths GPS altitude, which is far noisier than horizontal position, with a one-dimensional Kalman filter. */
class AltitudeFilter private constructor(sample: LocationSample) {

    var altitude: Double = sample.altitude
        private set

    private var variance = sample.verticalAccuracy * sample.verticalAccuracy
    private var timeSeconds = sample.timeSeconds

    fun update(sample: LocationSample) {
        val elapsed = sample.timeSeconds - timeSeconds
        if (!isUsable(sample) || elapsed <= 0) return
        variance += CLIMB_RATE * CLIMB_RATE * elapsed
        val gain = variance / (variance + sample.verticalAccuracy * sample.verticalAccuracy)
        altitude += gain * (sample.altitude - altitude)
        variance *= 1 - gain
        timeSeconds = sample.timeSeconds
    }

    companion object {
        /** Fixes with a vertical error above this (meters) are ignored. */
        const val MAX_USABLE_ACCURACY = 30.0

        /** How fast (m/s) the true altitude is expected to drift; climbing on foot rarely beats it. */
        const val CLIMB_RATE = 0.5

        /** Null when the fix has no usable altitude. */
        fun of(sample: LocationSample): AltitudeFilter? =
            if (isUsable(sample)) AltitudeFilter(sample) else null

        private fun isUsable(sample: LocationSample): Boolean =
            sample.verticalAccuracy > 0 && sample.verticalAccuracy <= MAX_USABLE_ACCURACY
    }
}

object RouteElevation {

    /**
     * Total climb in meters. A rise only counts once it clears [threshold] above the lowest point
     * since the last counted climb, so leftover altitude noise doesn't add up.
     */
    fun gain(route: List<RoutePoint>, threshold: Double = 3.0): Double {
        var gain = 0.0
        var base: Double? = null
        var segment: Int? = null
        for (point in route) {
            val altitude = point.altitude ?: continue
            if (point.segment != segment) {
                segment = point.segment
                base = altitude
            }
            val low = base ?: continue
            if (altitude - low >= threshold) {
                gain += altitude - low
                base = altitude
            } else if (altitude < low) {
                base = altitude
            }
        }
        return gain
    }
}
