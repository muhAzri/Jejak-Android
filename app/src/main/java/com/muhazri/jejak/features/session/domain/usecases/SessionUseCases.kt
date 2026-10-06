package com.muhazri.jejak.features.session.domain.usecases

import com.muhazri.jejak.features.home.domain.entities.SessionSummary
import com.muhazri.jejak.features.home.domain.repositories.SessionRepository
import com.muhazri.jejak.features.session.domain.entities.LocationSample
import com.muhazri.jejak.features.session.domain.entities.RecordedFix
import com.muhazri.jejak.features.session.domain.repositories.LocationTrackingService
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class TrackLocation @Inject constructor(private val service: LocationTrackingService) {
    operator fun invoke(): Flow<LocationSample> = service.start()
    fun stop() = service.stop()
}

class SaveSession @Inject constructor(private val repository: SessionRepository) {
    suspend operator fun invoke(session: SessionSummary, rawTrack: List<RecordedFix>) {
        repository.save(session)
        repository.saveRawTrack(rawTrack, session.id)
    }
}
