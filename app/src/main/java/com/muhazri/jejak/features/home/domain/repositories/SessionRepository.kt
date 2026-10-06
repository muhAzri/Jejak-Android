package com.muhazri.jejak.features.home.domain.repositories

import com.muhazri.jejak.features.home.domain.entities.SessionSummary
import com.muhazri.jejak.features.session.domain.entities.RecordedFix

interface SessionRepository {

    /** The most recently saved session, if any. */
    suspend fun latest(): SessionSummary?

    /** One saved session by id; null once it has been deleted. */
    suspend fun find(id: String): SessionSummary?

    suspend fun save(session: SessionSummary)

    /** Every fix as the device reported it, kept beside the session so it can be reprocessed later. */
    suspend fun saveRawTrack(track: List<RecordedFix>, id: String)

    suspend fun rawTrack(id: String): List<RecordedFix>

    /** Removes the session and its raw track. */
    suspend fun delete(id: String)
}
