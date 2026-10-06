package com.muhazri.jejak.features.session.domain.entities

import kotlinx.serialization.Serializable

/** A raw location fix from the device. Every reading is kept, so a session can be reprocessed later. */
@Serializable
data class LocationSample(
    val latitude: Double,
    val longitude: Double,
    /** Radius of uncertainty in meters; negative when the fix is invalid. */
    val horizontalAccuracy: Double,
    /** Epoch milliseconds. */
    val timestamp: Long,
    /** Doppler ground speed in m/s; negative when unknown. */
    val speed: Double = -1.0,
    /** Meters per second; negative when unknown. */
    val speedAccuracy: Double = -1.0,
    /** Direction of travel in degrees clockwise from north; negative when unknown. */
    val course: Double = -1.0,
    /** Degrees; negative when unknown. */
    val courseAccuracy: Double = -1.0,
    /** Meters above sea level; only meaningful when [verticalAccuracy] is positive. */
    val altitude: Double = 0.0,
    /** Meters; zero or negative when the altitude is invalid. */
    val verticalAccuracy: Double = -1.0,
) {
    /** The filter works in seconds; the entity keeps milliseconds to match [com.muhazri.jejak.features.home.domain.entities.RoutePoint]. */
    val timeSeconds: Double get() = timestamp / 1_000.0
}

/** A raw fix as it was recorded, with the recording stretch it belongs to. */
@Serializable
data class RecordedFix(
    val sample: LocationSample,
    /** Increments on every resume, so a replay restarts the filter where the live recording did. */
    val stretch: Int,
)
