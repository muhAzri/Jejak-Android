package com.muhazri.jejak.features.home.data.repositories

import android.util.Log
import com.muhazri.jejak.core.di.IoDispatcher
import com.muhazri.jejak.features.home.domain.entities.SessionSummary
import com.muhazri.jejak.features.home.domain.repositories.SessionRepository
import com.muhazri.jejak.features.session.domain.entities.RecordedFix
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Sessions live in one JSON file in app storage. Jejak has no server, so this is the only copy.
 * Raw tracks are large and rarely read, so each one gets its own file beside it.
 */
class SessionRepositoryImpl(
    private val file: File,
    private val json: Json,
    @param:IoDispatcher private val io: CoroutineDispatcher,
) : SessionRepository {

    private val rawTrackDirectory = File(file.parentFile, "${file.nameWithoutExtension}.raw")
    private val lock = Mutex()
    private var cache: List<SessionSummary>? = null

    override suspend fun latest(): SessionSummary? = all().maxByOrNull { it.startDate }

    override suspend fun find(id: String): SessionSummary? = all().firstOrNull { it.id == id }

    override suspend fun save(session: SessionSummary) {
        write(all().filter { it.id != session.id } + session)
    }

    override suspend fun saveRawTrack(track: List<RecordedFix>, id: String) {
        withContext(io) {
            try {
                rawTrackDirectory.mkdirs()
                writeAtomically(rawTrackFile(id), json.encodeToString(track))
            } catch (error: Exception) {
                Log.e(TAG, "Writing raw track failed", error)
            }
        }
    }

    override suspend fun rawTrack(id: String): List<RecordedFix> = withContext(io) {
        val file = rawTrackFile(id)
        if (!file.exists()) return@withContext emptyList()
        try {
            json.decodeFromString<List<RecordedFix>>(file.readText())
        } catch (error: Exception) {
            Log.e(TAG, "Reading raw track failed", error)
            emptyList()
        }
    }

    override suspend fun delete(id: String) {
        write(all().filter { it.id != id })
        withContext(io) { rawTrackFile(id).delete() }
    }

    private fun rawTrackFile(id: String) = File(rawTrackDirectory, "$id.json")

    private suspend fun all(): List<SessionSummary> = lock.withLock { load() }

    private suspend fun load(): List<SessionSummary> {
        cache?.let { return it }
        return withContext(io) {
            if (!file.exists()) {
                cache = emptyList()
                return@withContext emptyList()
            }
            try {
                json.decodeFromString<List<SessionSummary>>(file.readText()).also { cache = it }
            } catch (error: Exception) {
                // Leave the file untouched so a later version can still recover it.
                Log.e(TAG, "Reading sessions failed", error)
                emptyList()
            }
        }
    }

    private suspend fun write(sessions: List<SessionSummary>) = lock.withLock {
        withContext(io) {
            try {
                file.parentFile?.mkdirs()
                writeAtomically(file, json.encodeToString(sessions))
                cache = sessions
            } catch (error: Exception) {
                Log.e(TAG, "Writing sessions failed", error)
            }
        }
    }

    /** Writes through a sibling temp file so a crash mid-write can't leave a half-written session list. */
    private fun writeAtomically(target: File, contents: String) {
        val temp = File(target.parentFile, "${target.name}.tmp")
        temp.writeText(contents)
        if (!temp.renameTo(target)) {
            target.writeText(contents)
            temp.delete()
        }
    }

    private companion object {
        const val TAG = "SessionRepository"
    }
}
