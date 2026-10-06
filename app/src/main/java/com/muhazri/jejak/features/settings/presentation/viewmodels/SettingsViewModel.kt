package com.muhazri.jejak.features.settings.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.muhazri.jejak.features.onboarding.domain.entities.LocationPermission
import com.muhazri.jejak.features.onboarding.domain.usecases.GetLocationPermission
import com.muhazri.jejak.features.onboarding.domain.usecases.ObserveLocationPermission
import com.muhazri.jejak.features.onboarding.domain.usecases.RecordLocationPermissionResult
import com.muhazri.jejak.features.onboarding.domain.usecases.RefreshLocationPermission
import com.muhazri.jejak.features.settings.domain.entities.DistanceUnit
import com.muhazri.jejak.features.settings.domain.usecases.GetDistanceUnit
import com.muhazri.jejak.features.settings.domain.usecases.SetDistanceUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val unit: DistanceUnit = DistanceUnit.Kilometers,
    val locationPermission: LocationPermission = LocationPermission.NotDetermined,
) {
    /**
     * System settings only lists Location for an app that has asked at least once, so a never-asked
     * app shows the system dialog instead of sending the user there.
     */
    val locationRowRequestsPermission: Boolean
        get() = locationPermission == LocationPermission.NotDetermined
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getDistanceUnit: GetDistanceUnit,
    private val setDistanceUnit: SetDistanceUnit,
    private val getLocationPermission: GetLocationPermission,
    private val observeLocationPermission: ObserveLocationPermission,
    private val refreshLocationPermission: RefreshLocationPermission,
    private val recordLocationPermissionResult: RecordLocationPermissionResult,
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsUiState(unit = getDistanceUnit(), locationPermission = getLocationPermission()),
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private val _requests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** Emits when the screen should show the system location dialog. */
    val permissionRequests: Flow<Unit> = _requests.asSharedFlow()

    fun select(unit: DistanceUnit) {
        if (unit == _state.value.unit) return
        _state.update { it.copy(unit = unit) }
        setDistanceUnit(unit)
    }

    /** Re-reads the permission, e.g. after returning from system settings. */
    fun refresh() {
        refreshLocationPermission()
        _state.update { it.copy(locationPermission = getLocationPermission()) }
    }

    /** Follows permission changes for as long as the calling coroutine runs. */
    suspend fun observePermission() {
        observeLocationPermission().collect { permission ->
            _state.update { it.copy(locationPermission = permission) }
        }
    }

    fun requestPermission() {
        _requests.tryEmit(Unit)
    }

    fun onPermissionResult(isGranted: Boolean) {
        recordLocationPermissionResult(isGranted)
        _state.update { it.copy(locationPermission = getLocationPermission()) }
    }
}
