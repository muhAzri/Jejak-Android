package com.muhazri.jejak.features.onboarding.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.muhazri.jejak.features.onboarding.domain.usecases.CompleteOnboarding
import com.muhazri.jejak.features.onboarding.domain.usecases.RecordLocationPermissionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboarding: CompleteOnboarding,
    private val recordLocationPermissionResult: RecordLocationPermissionResult,
) : ViewModel() {

    private val _isRequestingPermission = MutableStateFlow(false)
    val isRequestingPermission: StateFlow<Boolean> = _isRequestingPermission.asStateFlow()

    private val _requests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** Emits when the screen should show the system location dialog. */
    val permissionRequests: Flow<Unit> = _requests.asSharedFlow()

    fun allowLocationTapped() {
        if (_isRequestingPermission.value) return
        _isRequestingPermission.value = true
        _requests.tryEmit(Unit)
    }

    /**
     * Onboarding is over whatever the dialog answered; Home handles a denied state with its own
     * notice rather than trapping the user here.
     */
    fun onPermissionResult(isGranted: Boolean) {
        recordLocationPermissionResult(isGranted)
        _isRequestingPermission.value = false
        completeOnboarding()
    }

    fun laterTapped() = completeOnboarding()
}
