package com.muhazri.jejak.features.session.domain.entities

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Estimates position and velocity from noisy fixes with a constant-velocity Kalman filter.
 * Works in meters on a plane tangent at the first fix, which is accurate over any session's span.
 */
class TrackFilter(sample: LocationSample, private val profile: MotionProfile) {

    private val originLatitude = sample.latitude
    private val originLongitude = sample.longitude
    private val metersPerDegreeLongitude = METERS_PER_DEGREE * cos(sample.latitude * PI / 180)

    private var east: Axis
    private var north: Axis

    /** Time of the last accepted fix, in seconds. */
    var timeSeconds: Double
        private set

    init {
        timeSeconds = sample.timeSeconds
        // Position is as good as the fix; velocity could be anything the activity allows.
        val axis = Axis(
            position = 0.0,
            velocity = 0.0,
            positionVariance = variance(sample.horizontalAccuracy),
            covariance = 0.0,
            velocityVariance = profile.maxSpeed * profile.maxSpeed,
        )
        east = axis
        north = axis
        velocityOf(sample)?.let { velocity ->
            east = east.correctVelocity(velocity.east, velocity.variance)
            north = north.correctVelocity(velocity.north, velocity.variance)
        }
    }

    val latitude: Double get() = originLatitude + north.position / METERS_PER_DEGREE
    val longitude: Double get() = originLongitude + east.position / metersPerDegreeLongitude

    /** Estimated ground speed in m/s. */
    val speed: Double get() = hypot(east.velocity, north.velocity)

    /** One standard deviation of the estimated position, in meters. */
    val positionAccuracy: Double get() = sqrt((east.positionVariance + north.positionVariance) / 2)

    /**
     * Folds the fix into the estimate. Returns false, leaving the estimate untouched, when the fix
     * is an outlier: out of order, a jump faster than the activity allows, or a change in motion
     * the track so far can't explain.
     */
    fun update(sample: LocationSample): Boolean {
        val elapsed = sample.timeSeconds - timeSeconds
        if (elapsed <= 0) return false
        val x = (sample.longitude - originLongitude) * metersPerDegreeLongitude
        val y = (sample.latitude - originLatitude) * METERS_PER_DEGREE
        val r = variance(sample.horizontalAccuracy)

        // Even granting the fix its whole error radius, it moved faster than anyone could.
        val jump = hypot(x - east.position, y - north.position) - max(0.0, sample.horizontalAccuracy)
        if (jump / elapsed > profile.maxSpeed) return false

        val q = profile.accelerationNoise * profile.accelerationNoise
        var nextEast = east.predict(elapsed, q)
        var nextNorth = north.predict(elapsed, q)
        val innovation = nextEast.normalizedInnovation(x, r) + nextNorth.normalizedInnovation(y, r)
        if (innovation > OUTLIER_GATE) return false

        nextEast = nextEast.correctPosition(x, r)
        nextNorth = nextNorth.correctPosition(y, r)
        velocityOf(sample)?.let { velocity ->
            nextEast = nextEast.correctVelocity(velocity.east, velocity.variance)
            nextNorth = nextNorth.correctVelocity(velocity.north, velocity.variance)
        }

        east = nextEast
        north = nextNorth
        timeSeconds = sample.timeSeconds
        return true
    }

    /** The Doppler velocity in the local plane, with the variance to trust it by. */
    private data class Velocity(val east: Double, val north: Double, val variance: Double)

    /** One axis of the state: position and velocity with their 2×2 covariance. */
    private data class Axis(
        val position: Double,
        val velocity: Double,
        val positionVariance: Double,
        val covariance: Double,
        val velocityVariance: Double,
    ) {
        /** Moves the estimate forward assuming constant velocity, perturbed by white-noise acceleration of variance [q]. */
        fun predict(dt: Double, q: Double) = Axis(
            position = position + velocity * dt,
            velocity = velocity,
            positionVariance = positionVariance + 2 * dt * covariance + dt * dt * velocityVariance +
                q * dt.pow(4) / 4,
            covariance = covariance + dt * velocityVariance + q * dt.pow(3) / 2,
            velocityVariance = velocityVariance + q * dt * dt,
        )

        fun normalizedInnovation(measured: Double, variance: Double): Double {
            val innovation = measured - position
            return innovation * innovation / (positionVariance + variance)
        }

        fun correctPosition(measured: Double, variance: Double): Axis {
            val s = positionVariance + variance
            val positionGain = positionVariance / s
            val velocityGain = covariance / s
            val innovation = measured - position
            return Axis(
                position = position + positionGain * innovation,
                velocity = velocity + velocityGain * innovation,
                positionVariance = positionVariance * (1 - positionGain),
                covariance = covariance * (1 - positionGain),
                velocityVariance = velocityVariance - velocityGain * covariance,
            )
        }

        fun correctVelocity(measured: Double, variance: Double): Axis {
            val s = velocityVariance + variance
            val positionGain = covariance / s
            val velocityGain = velocityVariance / s
            val innovation = measured - velocity
            return Axis(
                position = position + positionGain * innovation,
                velocity = velocity + velocityGain * innovation,
                positionVariance = positionVariance - positionGain * covariance,
                covariance = covariance * (1 - velocityGain),
                velocityVariance = velocityVariance * (1 - velocityGain),
            )
        }
    }

    companion object {
        /** Squared Mahalanobis distance from the prediction above which a fix is an outlier (χ², 2 dof, 99.9%). */
        const val OUTLIER_GATE = 13.8
        const val METERS_PER_DEGREE = 6_371_000.0 * PI / 180

        private fun variance(accuracy: Double): Double {
            val meters = max(accuracy, 1.0)
            return meters * meters
        }

        /**
         * The Doppler velocity, when the device reported one. Without a course the direction is unknown,
         * so each component is anywhere within ±speed around zero.
         */
        private fun velocityOf(sample: LocationSample): Velocity? {
            if (sample.speed < 0 || sample.speedAccuracy < 0) return null
            val speedVariance = max(sample.speedAccuracy * sample.speedAccuracy, 0.01)
            if (sample.course < 0 || sample.courseAccuracy < 0) {
                return Velocity(0.0, 0.0, speedVariance + sample.speed * sample.speed)
            }
            val course = sample.course * PI / 180
            val sideways = sample.speed * sample.courseAccuracy * PI / 180
            return Velocity(
                east = sample.speed * sin(course),
                north = sample.speed * cos(course),
                variance = speedVariance + sideways * sideways,
            )
        }
    }
}
