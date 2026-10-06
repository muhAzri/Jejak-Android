package com.muhazri.jejak.features.onboarding.domain.repositories

import com.muhazri.jejak.features.onboarding.domain.entities.LocationPermission
import kotlinx.coroutines.flow.Flow

interface LocationPermissionService {

    fun current(): LocationPermission

    /** Emits the current status, then every change (the system dialog, or a return from Settings). */
    fun updates(): Flow<LocationPermission>

    /** Re-reads the system status. Android has no permission callback, so screens refresh on resume. */
    fun refresh()

    /**
     * Records what the system dialog answered. Showing it is the Activity's job, so the service only
     * learns the outcome; a grant is remembered so a later "only this time" expiry reads as allowed-once.
     */
    fun onRequestResult(isGranted: Boolean)
}
