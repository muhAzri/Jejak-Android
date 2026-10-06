package com.muhazri.jejak.session

import com.muhazri.jejak.features.home.data.repositories.SessionRepositoryImpl
import com.muhazri.jejak.features.home.domain.entities.ActivityType
import com.muhazri.jejak.features.home.domain.entities.RoutePoint
import com.muhazri.jejak.features.home.domain.entities.SessionSummary
import com.muhazri.jejak.features.session.domain.entities.LocationSample
import com.muhazri.jejak.features.session.domain.entities.RecordedFix
import java.io.File
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SessionRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val json = Json { ignoreUnknownKeys = true }

    private fun repository(file: File) = SessionRepositoryImpl(
        file = file,
        json = json,
        io = kotlinx.coroutines.Dispatchers.Unconfined,
    )

    private fun sessionsFile() = File(folder.newFolder(), "sessions.json")

    private val older = SessionSummary(
        activity = ActivityType.Walk,
        startDate = 100_000,
        distanceMeters = 800.0,
        durationSeconds = 600.0,
    )

    private val newer = SessionSummary(
        activity = ActivityType.Run,
        startDate = 5_000_000,
        distanceMeters = 5_240.0,
        durationSeconds = 1_721.0,
        route = listOf(RoutePoint(latitude = -6.2, longitude = 106.8, timestamp = 5_000_000)),
    )

    @Test
    fun `saves and reloads the latest session`() = runTest {
        val file = sessionsFile()
        repository(file).apply {
            save(newer)
            save(older)
        }

        assertEquals(newer, repository(file).latest())
    }

    @Test
    fun `deleting the latest session falls back to the one before`() = runTest {
        val file = sessionsFile()
        val repository = repository(file)
        repository.save(older)
        repository.save(newer)

        repository.delete(newer.id)
        assertEquals(older, repository(file).latest())

        repository.delete(older.id)
        assertNull(repository(file).latest())
    }

    @Test
    fun `stores raw tracks beside sessions and deletes them together`() = runTest {
        val file = sessionsFile()
        val session = SessionSummary(
            activity = ActivityType.Run,
            startDate = 5_000_000,
            distanceMeters = 500.0,
            durationSeconds = 200.0,
        )
        val track = listOf(
            RecordedFix(
                sample = LocationSample(
                    latitude = -6.2,
                    longitude = 106.8,
                    horizontalAccuracy = 5.0,
                    timestamp = 5_000_000,
                    speed = 3.0,
                    speedAccuracy = 0.4,
                    course = 90.0,
                    courseAccuracy = 12.0,
                    altitude = 8.0,
                    verticalAccuracy = 4.0,
                ),
                stretch = 0,
            ),
        )

        val repository = repository(file)
        repository.save(session)
        repository.saveRawTrack(track, session.id)

        assertEquals(track, repository(file).rawTrack(session.id))

        repository.delete(session.id)
        assertTrue(repository.rawTrack(session.id).isEmpty())
    }

    @Test
    fun `finds one session by id`() = runTest {
        val file = sessionsFile()
        val repository = repository(file)
        repository.save(older)
        repository.save(newer)

        assertEquals(older, repository(file).find(older.id))
        assertNull(repository(file).find("missing"))
    }

    @Test
    fun `reads sessions saved before altitude was recorded`() {
        val stored = """
            [{"id":"9b1deb4d","activity":"Run","startDate":0,"endDate":60000,"distanceMeters":200.0,
              "durationSeconds":60.0,
              "route":[{"latitude":1.0,"longitude":2.0,"timestamp":0,"isEstimated":false,"segment":0}]}]
        """.trimIndent()

        val sessions = json.decodeFromString<List<SessionSummary>>(stored)
        assertNull(sessions.first().route.first().altitude)
    }
}
