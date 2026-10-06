package com.muhazri.jejak.session

import com.muhazri.jejak.features.home.domain.entities.ActivityType
import com.muhazri.jejak.features.session.domain.entities.Geo
import com.muhazri.jejak.features.session.domain.entities.LocationSample
import com.muhazri.jejak.features.session.domain.entities.RouteElevation
import com.muhazri.jejak.features.session.domain.entities.SessionRecorder
import com.muhazri.jejak.features.session.domain.entities.TrackFilter
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionRecorderTest {

    @Test
    fun `accumulates distance along the route`() {
        val recorder = SessionRecorder(ActivityType.Run)
        for (step in 0..10) recorder.record(sample(step, step * 4.0))

        assertTrue(recorder.isMoving)
        assertEquals(0.0, recorder.route.first().latitude, 0.0)
        // The smoothed estimate trails the last fix by a little.
        assertTrue(abs(recorder.distanceMeters - metersPerStep * 10) < 3)
    }

    @Test
    fun `drops imprecise jittery and impossible fixes`() {
        val recorder = SessionRecorder(ActivityType.Run)
        recorder.record(sample(0, 0.0))

        val imprecise = recorder.record(sample(1, 4.0, accuracy = 100.0))
        val jitter = recorder.record(
            LocationSample(latitude = 0.00001, longitude = 0.0, horizontalAccuracy = 5.0, timestamp = at(5.0)),
        ) // ~1 m
        val jump = recorder.record(sample(50, 6.0)) // 550 m in 6 s

        assertFalse(imprecise)
        assertFalse(jitter)
        assertFalse(jump)
        assertEquals(1, recorder.route.size)
        assertEquals(0.0, recorder.distanceMeters, 0.0)
    }

    @Test
    fun `marks weak fixes as estimated`() {
        val recorder = SessionRecorder(ActivityType.Run)
        recorder.record(sample(0, 0.0))
        for (step in 1..8) recorder.record(sample(step, step * 4.0, accuracy = 40.0))

        assertFalse(recorder.route.first().isEstimated)
        assertTrue(recorder.route.size > 1)
        assertTrue(recorder.route.drop(1).all { it.isEstimated })
    }

    @Test
    fun `does not count the gap across a pause`() {
        val recorder = SessionRecorder(ActivityType.Run)
        for (step in 0..5) recorder.record(sample(step, step * 4.0))
        recorder.startNewSegment()
        for (step in 20..25) recorder.record(sample(step, step * 4.0))

        assertTrue(abs(recorder.distanceMeters - metersPerStep * 10) < 4)
        assertEquals(setOf(0, 1), recorder.route.map { it.segment }.toSet())
    }

    @Test
    fun `standing still adds no distance`() {
        val noise = Noise(7uL)
        val withoutDoppler = SessionRecorder(ActivityType.Run)
        val withDoppler = SessionRecorder(ActivityType.Run)
        for (second in 0 until 300) {
            val fix = noisyFix(east = 0.0, north = 0.0, seconds = second.toDouble(), sigma = 5.0, noise = noise)
            withoutDoppler.record(fix)
            withDoppler.record(
                fix.copy(speed = abs(noise.gaussian(0.15)), speedAccuracy = 0.3),
            )
        }

        // Summing the raw fixes would make this a ~2 km "run".
        assertTrue(withoutDoppler.distanceMeters < 30)
        assertEquals(0.0, withDoppler.distanceMeters, 0.0)
        assertFalse(withDoppler.isMoving)
    }

    @Test
    fun `follows corners without cutting them`() {
        val noise = Noise(5uL)
        val recorder = SessionRecorder(ActivityType.Run)
        // Three laps of a 100 m square at 3 m/s.
        for (second in 0..400) {
            val along = (second * 3.0) % 400
            val side = (along / 100).toInt()
            val offset = along - side * 100
            val (east, north) = when (side) {
                0 -> offset to 0.0
                1 -> 100.0 to offset
                2 -> (100 - offset) to 100.0
                else -> 0.0 to (100 - offset)
            }
            recorder.record(noisyFix(east, north, second.toDouble(), sigma = 4.0, noise = noise))
        }

        assertTrue(abs(recorder.distanceMeters - 1200) < 1200 * 0.04)
    }

    @Test
    fun `noisy run measures close to the true distance`() {
        val noise = Noise(42uL)
        val recorder = SessionRecorder(ActivityType.Run)
        var raw = 0.0
        var previous: LocationSample? = null
        // 10 minutes at 3 m/s: 1800 m.
        for (second in 0..600) {
            val fix = noisyFix(second * 3.0, 0.0, second.toDouble(), sigma = 4.0, noise = noise)
            previous?.let {
                raw += Geo.distance(it.latitude, it.longitude, fix.latitude, fix.longitude)
            }
            previous = fix
            recorder.record(fix)
        }

        assertTrue(abs(recorder.distanceMeters - 1800) < 1800 * 0.03)
        assertTrue(raw > 1800 * 1.3) // what the old naive sum would have reported
    }

    @Test
    fun `stop at a traffic light is not counted`() {
        val noise = Noise(3uL)
        val recorder = SessionRecorder(ActivityType.Run)
        var east = 0.0
        for (second in 0..180) {
            // Run, wait a minute at the light, run on: 120 s × 3 m/s = 360 m.
            if (second !in 60 until 120) east += 3
            recorder.record(noisyFix(east, 0.0, second.toDouble(), sigma = 4.0, noise = noise))
        }

        assertTrue(abs(recorder.distanceMeters - 360) < 360 * 0.05)
    }

    @Test
    fun `rejects a GPS spike mid run`() {
        val recorder = SessionRecorder(ActivityType.Run)
        for (second in 0..30) {
            val east = if (second == 15) 45 + 60.0 else second * 3.0 // one fix 60 m off the line
            recorder.record(
                LocationSample(
                    latitude = 0.0,
                    longitude = east / TrackFilter.METERS_PER_DEGREE,
                    horizontalAccuracy = 5.0,
                    timestamp = at(second.toDouble()),
                ),
            )
        }

        assertTrue(abs(recorder.distanceMeters - 90) < 5)
    }

    @Test
    fun `plausible speed depends on the activity`() {
        val fast = LocationSample(
            latitude = 0.0,
            longitude = 0.0,
            horizontalAccuracy = 5.0,
            timestamp = at(0.0),
            speed = 7.0,
            speedAccuracy = 0.5,
        )

        assertTrue(SessionRecorder(ActivityType.Run).record(fast))
        assertFalse(SessionRecorder(ActivityType.Walk).record(fast))
    }

    @Test
    fun `doppler speed tells moving from standing`() {
        val recorder = SessionRecorder(ActivityType.Walk)
        for (second in 0..20) {
            recorder.record(
                LocationSample(
                    latitude = second * 1.4 / TrackFilter.METERS_PER_DEGREE,
                    longitude = 0.0,
                    horizontalAccuracy = 5.0,
                    timestamp = at(second.toDouble()),
                    speed = 1.4,
                    speedAccuracy = 0.3,
                    course = 0.0,
                    courseAccuracy = 10.0,
                ),
            )
        }

        assertTrue(recorder.isMoving)
        assertTrue(abs(recorder.distanceMeters - 28) < 3)
    }

    @Test
    fun `keeps every raw fix and replays to the same result`() {
        val noise = Noise(11uL)
        val recorder = SessionRecorder(ActivityType.Run)
        for (second in 0..60) {
            recorder.record(noisyFix(second * 3.0, 0.0, second.toDouble(), sigma = 4.0, noise = noise))
        }
        recorder.startNewSegment()
        for (second in 200..260) {
            recorder.record(noisyFix(second * 3.0, 0.0, second.toDouble(), sigma = 4.0, noise = noise))
        }
        recorder.finish()

        assertEquals(122, recorder.rawTrack.size)

        val replayed = SessionRecorder.replaying(ActivityType.Run, recorder.rawTrack)
        assertEquals(recorder.route, replayed.route)
        assertEquals(recorder.distanceMeters, replayed.distanceMeters, 0.0)
    }

    @Test
    fun `smooths altitude into the route`() {
        val noise = Noise(5uL)
        val recorder = SessionRecorder(ActivityType.Run)
        for (second in 0..300) {
            // Climb 30 m over the run; GPS altitude is off by several meters each fix.
            val altitude = second / 10.0 + noise.gaussian(6.0)
            recorder.record(
                noisyFix(
                    east = second * 3.0,
                    north = 0.0,
                    seconds = second.toDouble(),
                    sigma = 4.0,
                    noise = noise,
                    altitude = altitude,
                    verticalAccuracy = 8.0,
                ),
            )
        }

        assertTrue(abs(RouteElevation.gain(recorder.route) - 30) < 8)
    }
}
