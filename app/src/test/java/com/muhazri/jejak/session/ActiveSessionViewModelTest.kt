package com.muhazri.jejak.session

import com.muhazri.jejak.core.time.TimeSource
import com.muhazri.jejak.features.home.domain.entities.ActivityType
import com.muhazri.jejak.features.home.domain.entities.SessionSummary
import com.muhazri.jejak.features.home.domain.repositories.SessionRepository
import com.muhazri.jejak.features.session.domain.entities.GPSSignal
import com.muhazri.jejak.features.session.domain.entities.LocationSample
import com.muhazri.jejak.features.session.domain.entities.RecordedFix
import com.muhazri.jejak.features.session.domain.entities.SessionPhase
import com.muhazri.jejak.features.session.domain.repositories.LocationTrackingService
import com.muhazri.jejak.features.session.domain.usecases.SaveSession
import com.muhazri.jejak.features.session.domain.usecases.TrackLocation
import com.muhazri.jejak.features.session.presentation.viewmodels.ActiveSessionViewModel
import com.muhazri.jejak.features.settings.domain.entities.DistanceUnit
import com.muhazri.jejak.features.settings.domain.repositories.SettingsRepository
import com.muhazri.jejak.features.settings.domain.usecases.GetDistanceUnit
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveSessionViewModelTest {

    private class FakeTracking : LocationTrackingService {
        override fun start(): Flow<LocationSample> = emptyFlow()
        override fun stop() = Unit
    }

    private class FakeSettings : SettingsRepository {
        override fun distanceUnit() = DistanceUnit.Kilometers
        override fun setDistanceUnit(unit: DistanceUnit) = Unit
    }

    private class FakeSessions : SessionRepository {
        val saved = mutableListOf<SessionSummary>()
        val rawTracks = mutableMapOf<String, List<RecordedFix>>()

        override suspend fun latest() = saved.lastOrNull()
        override suspend fun find(id: String) = saved.firstOrNull { it.id == id }
        override suspend fun save(session: SessionSummary) {
            saved.add(session)
        }

        override suspend fun saveRawTrack(track: List<RecordedFix>, id: String) {
            rawTracks[id] = track
        }

        override suspend fun rawTrack(id: String) = rawTracks[id].orEmpty()
        override suspend fun delete(id: String) {
            saved.removeAll { it.id == id }
        }
    }

    private var now = START_MILLIS
    private val sessions = FakeSessions()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeModel() = ActiveSessionViewModel(
        activity = ActivityType.Run,
        getDistanceUnit = GetDistanceUnit(FakeSettings()),
        trackLocation = TrackLocation(FakeTracking()),
        saveSession = SaveSession(sessions),
        time = TimeSource { now },
    )

    /** Moves 1 step (~11 m) every 4 s, as long as [steps]. */
    private fun walk(model: ActiveSessionViewModel, from: Int, steps: Int) {
        for (step in from until from + steps) {
            now += 4_000
            model.receive(
                LocationSample(
                    latitude = step * 0.0001,
                    longitude = 0.0,
                    horizontalAccuracy = 5.0,
                    timestamp = now,
                ),
            )
        }
    }

    @Test
    fun `starts recording on the first good fix`() {
        val model = makeModel()
        model.receive(LocationSample(0.0, 0.0, horizontalAccuracy = 50.0, timestamp = now))
        assertEquals(SessionPhase.Searching, model.state.value.phase)
        assertEquals(GPSSignal.Weak, model.state.value.signal)

        model.receive(LocationSample(0.0, 0.0, horizontalAccuracy = 5.0, timestamp = now))
        assertEquals(SessionPhase.Recording, model.state.value.phase)
        assertEquals(GPSSignal.Good, model.state.value.signal)
    }

    @Test
    fun `paused time is not counted`() {
        val model = makeModel()
        model.startRecording()
        now += 60_000
        model.pause()
        now += 300_000
        model.tick()
        assertEquals(300.0, model.state.value.pausedForSeconds, 0.0)

        model.resume()
        now += 30_000
        model.tick()
        assertEquals(90.0, model.state.value.durationSeconds, 0.0)
    }

    @Test
    fun `too short sessions ask to keep going`() {
        val model = makeModel()
        walk(model, from = 0, steps = 5)
        model.finish()

        assertTrue(model.state.value.isShowingTooShort)
        assertEquals(SessionPhase.Recording, model.state.value.phase)

        model.keepGoing()
        assertFalse(model.state.value.isShowingTooShort)
    }

    @Test
    fun `saves a long enough session`() = runTest {
        val model = makeModel()
        walk(model, from = 0, steps = 20) // ~210 m in 76 s
        model.pause()
        model.finish()
        assertEquals(SessionPhase.Finished, model.state.value.phase)

        model.save()

        val saved = sessions.saved.single()
        assertEquals(ActivityType.Run, saved.activity)
        assertTrue(abs(saved.distanceMeters - metersPerStep * 19) < 2)
        assertEquals(76.0, saved.durationSeconds, 0.0)
        assertEquals(20, sessions.rawTrack(saved.id).size)
    }

    @Test
    fun `weak when fixes stop arriving`() {
        val model = makeModel()
        walk(model, from = 0, steps = 3)
        assertEquals(GPSSignal.Good, model.state.value.signal)

        now += 11_000
        model.tick()
        assertEquals(GPSSignal.Weak, model.state.value.signal)
        assertNull(model.state.value.currentPace)
    }
}
