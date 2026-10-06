package com.muhazri.jejak.features.home.domain.entities

/** One recorded location along a session's route. */
data class RoutePoint(
    val latitude: Double,
    val longitude: Double,
    /** Epoch milliseconds. */
    val timestamp: Long,
    /** Recorded with a weak GPS fix; drawn dotted. */
    val isEstimated: Boolean = false,
    /** Increments after every pause (or a GPS jump too far to bridge), so the route is not joined across it. */
    val segment: Int = 0,
    /** Smoothed altitude in meters; null when GPS gave none. */
    val altitude: Double? = null,
)
