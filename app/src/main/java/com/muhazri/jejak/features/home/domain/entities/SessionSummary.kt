package com.muhazri.jejak.features.home.domain.entities

import java.util.UUID

/** A saved session, as shown on Home and stored on the device. All instants are epoch milliseconds. */
data class SessionSummary(
    val activity: ActivityType,
    val startDate: Long,
    val distanceMeters: Double,
    /** Moving time in seconds: pauses are not counted. */
    val durationSeconds: Double,
    val endDate: Long = startDate + (durationSeconds * 1_000).toLong(),
    val route: List<RoutePoint> = emptyList(),
    val id: String = UUID.randomUUID().toString(),
) {
    /** Seconds per meter; null when no distance was covered. */
    val pace: Double? get() = if (distanceMeters > 0) durationSeconds / distanceMeters else null
}
