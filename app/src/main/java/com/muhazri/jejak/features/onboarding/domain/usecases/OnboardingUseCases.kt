package com.muhazri.jejak.features.onboarding.domain.usecases

import com.muhazri.jejak.features.onboarding.domain.entities.LocationPermission
import com.muhazri.jejak.features.onboarding.domain.repositories.LocationPermissionService
import com.muhazri.jejak.features.onboarding.domain.repositories.OnboardingRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetOnboardingStatus @Inject constructor(private val repository: OnboardingRepository) {
    operator fun invoke(): Boolean = repository.isCompleted()
}

class CompleteOnboarding @Inject constructor(private val repository: OnboardingRepository) {
    operator fun invoke() = repository.markCompleted()
}

class GetLocationPermission @Inject constructor(private val service: LocationPermissionService) {
    operator fun invoke(): LocationPermission = service.current()
}

class ObserveLocationPermission @Inject constructor(private val service: LocationPermissionService) {
    operator fun invoke(): Flow<LocationPermission> = service.updates()
}

/** Re-reads the status after the system dialog or a trip to Settings. */
class RefreshLocationPermission @Inject constructor(private val service: LocationPermissionService) {
    operator fun invoke() = service.refresh()
}

/**
 * Closes the permission request the screen opened. The system dialog needs an Activity, so the screen
 * launches it and hands the answer back here.
 */
class RecordLocationPermissionResult @Inject constructor(private val service: LocationPermissionService) {
    operator fun invoke(isGranted: Boolean) = service.onRequestResult(isGranted)
}
