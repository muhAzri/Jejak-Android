package com.muhazri.jejak.features.home.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhazri.jejak.features.home.domain.entities.ActivityType
import com.muhazri.jejak.features.home.domain.entities.SessionSummary
import com.muhazri.jejak.features.home.domain.usecases.DeleteSession
import com.muhazri.jejak.features.home.domain.usecases.GetLastSession
import com.muhazri.jejak.features.onboarding.domain.entities.LocationPermission
import com.muhazri.jejak.features.onboarding.domain.usecases.GetLocationPermission
import com.muhazri.jejak.features.onboarding.domain.usecases.ObserveLocationPermission
import com.muhazri.jejak.features.onboarding.domain.usecases.RecordLocationPermissionResult
import com.muhazri.jejak.features.onboarding.domain.usecases.RefreshLocationPermission
import com.muhazri.jejak.features.settings.domain.entities.DistanceUnit
import com.muhazri.jejak.features.settings.domain.usecases.GetDistanceUnit
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
import kotlinx.coroutines.launch

data class HomeUiState(
    val permission: LocationPermission = LocationPermission.NotDetermined,
    val lastSession: SessionSummary? = null,
    val unit: DistanceUnit = DistanceUnit.Kilometers,
    val isRequestingPermission: Boolean = false,
)

/** Asks the screen for something only an Activity can do, or that the navigator owns. */
sealed interface HomeEvent {
    /** Show the system location dialog. */
    data object RequestLocationPermission : HomeEvent

    /** Location is sorted out; open the recording screen. */
    data class StartActivity(val activity: ActivityType) : HomeEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getLastSession: GetLastSession,
    private val deleteSession: DeleteSession,
    private val getDistanceUnit: GetDistanceUnit,
    private val getLocationPermission: GetLocationPermission,
    private val observeLocationPermission: ObserveLocationPermission,
    private val refreshLocationPermission: RefreshLocationPermission,
    private val recordLocationPermissionResult: RecordLocationPermissionResult,
) : ViewModel() {

    private val _state = MutableStateFlow(
        HomeUiState(permission = getLocationPermission(), unit = getDistanceUnit()),
    )
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: Flow<HomeEvent> = _events.asSharedFlow()

    /** The activity waiting on the permission dialog, if the user tapped a start card without it. */
    private var pendingActivity: ActivityType? = null

    init {
        refresh()
    }

    /** Starting needs location; when it is off the cards are locked and the notice explains why. */
    val canStart: Boolean get() = _state.value.permission != LocationPermission.Denied

    /** Re-reads everything Home shows: on resume, after Settings, and when returning from system settings. */
    fun refresh() {
        refreshLocationPermission()
        _state.update {
            it.copy(permission = getLocationPermission(), unit = getDistanceUnit())
        }
        viewModelScope.launch {
            val session = getLastSession()
            _state.update { it.copy(lastSession = session) }
        }
    }

    /** Removes a saved session for good; the one before it, if any, becomes the last session. */
    fun delete(session: SessionSummary) {
        viewModelScope.launch {
            deleteSession(session.id)
            val latest = getLastSession()
            _state.update { it.copy(lastSession = latest) }
        }
    }

    /** Follows permission changes for as long as the calling coroutine runs. */
    suspend fun observePermission() {
        observeLocationPermission().collect { permission ->
            _state.update { it.copy(permission = permission) }
        }
    }

    /** Shows the system dialog while the platform still allows asking. */
    fun requestPermission() {
        val state = _state.value
        if (state.permission.isGranted || state.permission == LocationPermission.Denied) return
        if (state.isRequestingPermission) return
        _state.update { it.copy(isRequestingPermission = true) }
        _events.tryEmit(HomeEvent.RequestLocationPermission)
    }

    /** The system dialog closed; a grant releases whichever start card was waiting on it. */
    fun onPermissionResult(isGranted: Boolean) {
        recordLocationPermissionResult(isGranted)
        _state.update {
            it.copy(permission = getLocationPermission(), isRequestingPermission = false)
        }
        val pending = pendingActivity
        pendingActivity = null
        if (pending != null && _state.value.permission.isGranted) {
            _events.tryEmit(HomeEvent.StartActivity(pending))
        }
    }

    /** A start card was tapped: asks for location if needed, then opens the session. */
    fun start(activity: ActivityType) {
        if (!canStart || _state.value.isRequestingPermission) return
        if (_state.value.permission.isGranted) {
            _events.tryEmit(HomeEvent.StartActivity(activity))
            return
        }
        pendingActivity = activity
        requestPermission()
    }
}
