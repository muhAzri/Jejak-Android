package com.muhazri.jejak.features.session.domain.entities

import com.muhazri.jejak.features.home.domain.entities.ActivityType

/** How someone can plausibly move during an activity; tunes the filter and the stop detection. */
data class MotionProfile(
    /** Faster than anyone doing this activity (m/s); anything above is a GPS jump. */
    val maxSpeed: Double,
    /** Typical change in velocity per second (m/s²): how quickly the filter lets the estimate turn or speed up. */
    val accelerationNoise: Double,
    /** Estimated speed (m/s) needed to count as moving again after a stop. */
    val startSpeed: Double,
    /** Estimated speed (m/s) below which the runner counts as stopped. */
    val stopSpeed: Double,
    /** Meters the estimate must leave the stop by before movement counts; never less than the fix's accuracy. */
    val stopRadius: Double,
    /** Minimum meters between route points while moving. */
    val pointSpacing: Double,
)

val ActivityType.motionProfile: MotionProfile
    get() = when (this) {
        ActivityType.Run -> MotionProfile(
            maxSpeed = 12.0,
            accelerationNoise = 0.5,
            startSpeed = 0.8,
            stopSpeed = 0.5,
            stopRadius = 8.0,
            pointSpacing = 5.0,
        )

        ActivityType.Walk -> MotionProfile(
            maxSpeed = 5.0,
            accelerationNoise = 0.3,
            startSpeed = 0.5,
            stopSpeed = 0.3,
            stopRadius = 6.0,
            pointSpacing = 3.0,
        )
    }
