package com.muhazri.jejak.features.session.domain.entities

import com.muhazri.jejak.features.home.domain.entities.ActivityType
import com.muhazri.jejak.features.home.domain.entities.RoutePoint
import kotlin.math.max

/**
 * Turns location fixes into a route and a distance. Pure logic; the view model decides when to feed it.
 *
 * Each fix goes through: quality check → outlier rejection and Kalman smoothing ([TrackFilter])
 * → stop detection → distance. The raw fixes are kept as they came, so a session can be replayed.
 */
class SessionRecorder(val activity: ActivityType) {

    private val _route = mutableListOf<RoutePoint>()
    val route: List<RoutePoint> get() = _route

    var segment = 0
        private set

    /** Every fix handed to [record], accepted or not. */
    private val _rawTrack = mutableListOf<RecordedFix>()
    val rawTrack: List<RecordedFix> get() = _rawTrack

    /** False while standing still (or before moving at all); GPS wander then neither draws nor counts. */
    var isMoving = false
        private set

    private val profile = activity.motionProfile
    private var stretch = 0
    private var filter: TrackFilter? = null
    private var altitudeFilter: AltitudeFilter? = null
    private var outliersInARow = 0
    private var movingFixesInARow = 0

    /** Where the runner stopped. */
    private var stop: RoutePoint? = null

    /** The latest smoothed position; while moving it runs up to one point spacing ahead of the route. */
    private var latest: RoutePoint? = null
    private var routeDistance = 0.0

    /** Meters covered, including the stretch since the last route point. */
    val distanceMeters: Double
        get() {
            val latest = latest ?: return routeDistance
            val last = _route.lastOrNull() ?: return routeDistance
            if (!isMoving || last.segment != latest.segment) return routeDistance
            return routeDistance + Geo.distance(last, latest)
        }

    /** The smoothed current position, carrying the latest fix's accuracy and time. */
    val estimatedPosition: LocationSample?
        get() {
            val latest = latest ?: return null
            val last = _rawTrack.lastOrNull()?.sample ?: return null
            return LocationSample(
                latitude = latest.latitude,
                longitude = latest.longitude,
                horizontalAccuracy = last.horizontalAccuracy,
                timestamp = last.timestamp,
            )
        }

    /** Feeds a fix through the pipeline. Returns whether it added a point to the route. */
    fun record(sample: LocationSample): Boolean {
        _rawTrack.add(RecordedFix(sample, stretch))
        if (sample.horizontalAccuracy < 0 || sample.horizontalAccuracy > MAX_USABLE_ACCURACY) return false
        if (sample.speed > profile.maxSpeed) return false

        val next = filter
        if (next == null ||
            sample.timeSeconds - next.timeSeconds > MAX_GAP ||
            outliersInARow >= MAX_CONSECUTIVE_OUTLIERS
        ) {
            return restart(sample)
        }
        if (!next.update(sample)) {
            outliersInARow += 1
            return false
        }
        outliersInARow = 0
        val altitude = altitudeFilter
        if (altitude != null) altitude.update(sample) else altitudeFilter = AltitudeFilter.of(sample)

        val point = makePoint(next.latitude, next.longitude, sample)
        latest = point
        if (isMoving) {
            val last = _route.lastOrNull()
            if (next.speed < profile.stopSpeed) {
                isMoving = false
                stop = point
            } else if (last != null &&
                Geo.distance(last, point) < max(profile.pointSpacing, 3 * next.positionAccuracy)
            ) {
                // Chords well beyond the estimate's own wobble keep leftover jitter from adding up.
                return false
            }
        } else {
            // Leaving the stop: past both the GPS's own wander and the starting speed, for a few fixes running.
            val stop = stop
            if (stop == null ||
                next.speed < profile.startSpeed ||
                Geo.distance(stop, point) <= max(profile.stopRadius, sample.horizontalAccuracy)
            ) {
                movingFixesInARow = 0
                return false
            }
            movingFixesInARow += 1
            if (movingFixesInARow < FIXES_TO_START) return false
            movingFixesInARow = 0
            isMoving = true
        }
        append(point)
        return true
    }

    /** Called on resume: the gap while paused is neither drawn nor counted. */
    fun startNewSegment() {
        commitLatest()
        stretch += 1
        filter = null
        latest = null
        isMoving = false
        if (_route.lastOrNull()?.segment == segment) segment += 1
    }

    /** Called when the session ends: draws the route up to the final position. */
    fun finish() {
        commitLatest()
    }

    private fun commitLatest() {
        val latest = latest ?: return
        if (!isMoving || _route.lastOrNull() == latest) return
        append(latest)
    }

    /**
     * Starts the filter afresh from this fix: at the beginning, after a resume, after a signal gap,
     * or once GPS has disagreed with the estimate for too long.
     */
    private fun restart(sample: LocationSample): Boolean {
        commitLatest()
        filter = TrackFilter(sample, profile)
        altitudeFilter = AltitudeFilter.of(sample)
        outliersInARow = 0
        movingFixesInARow = 0
        isMoving = false

        val last = _route.lastOrNull()
        if (last != null && last.segment == segment) {
            val step = Geo.distance(last.latitude, last.longitude, sample.latitude, sample.longitude)
            if (step <= max(profile.stopRadius, sample.horizontalAccuracy)) {
                // Still where the route left off.
                stop = last
                latest = last
                return false
            }
            // Bridge the gap only at a speed the activity allows; otherwise break the route here.
            val elapsed = sample.timeSeconds - last.timestamp / 1_000.0
            if (elapsed <= 0 || step / elapsed > profile.maxSpeed) segment += 1
        }
        val point = makePoint(sample.latitude, sample.longitude, sample)
        stop = point
        latest = point
        append(point)
        return true
    }

    private fun append(point: RoutePoint) {
        val last = _route.lastOrNull()
        if (last != null && last.segment == point.segment) routeDistance += Geo.distance(last, point)
        _route.add(point)
    }

    private fun makePoint(latitude: Double, longitude: Double, sample: LocationSample) = RoutePoint(
        latitude = latitude,
        longitude = longitude,
        timestamp = sample.timestamp,
        isEstimated = GPSSignal.of(sample.horizontalAccuracy) != GPSSignal.Good,
        segment = segment,
        altitude = altitudeFilter?.altitude,
    )

    companion object {
        /** Fixes less precise than this are dropped entirely. */
        const val MAX_USABLE_ACCURACY = 65.0

        /** After this long (seconds) without an accepted fix, the estimate is stale and the filter starts over. */
        const val MAX_GAP = 10.0

        /** Rejections in a row after which GPS is trusted over the filter's own estimate. */
        const val MAX_CONSECUTIVE_OUTLIERS = 5

        /** Fixes in a row that must look like movement before a stop ends, so one wild estimate doesn't. */
        const val FIXES_TO_START = 3

        /** Reprocesses a recorded session with the current algorithm. */
        fun replaying(activity: ActivityType, fixes: List<RecordedFix>): SessionRecorder {
            val recorder = SessionRecorder(activity)
            for (fix in fixes) {
                while (recorder.stretch < fix.stretch) recorder.startNewSegment()
                recorder.record(fix.sample)
            }
            recorder.finish()
            return recorder
        }
    }
}
