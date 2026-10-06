package com.muhazri.jejak.features.session.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhazri.jejak.core.time.TimeSource
import com.muhazri.jejak.features.home.domain.entities.ActivityType
import com.muhazri.jejak.features.home.domain.entities.SessionSummary
import com.muhazri.jejak.features.session.domain.entities.GPSSignal
import com.muhazri.jejak.features.session.domain.entities.LocationSample
import com.muhazri.jejak.features.session.domain.entities.RoutePace
import com.muhazri.jejak.features.session.domain.entities.SessionPhase
import com.muhazri.jejak.features.session.domain.entities.SessionRecorder
import com.muhazri.jejak.features.session.domain.usecases.SaveSession
import com.muhazri.jejak.features.session.domain.usecases.TrackLocation
import com.muhazri.jejak.features.session.presentation.screens.ActiveSessionUiState
import com.muhazri.jejak.features.session.presentation.screens.SessionReport
import com.muhazri.jejak.features.settings.domain.usecases.GetDistanceUnit
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ActiveSessionViewModel.Factory::class)
class ActiveSessionViewModel @AssistedInject constructor(
    @Assisted val activity: ActivityType,
    getDistanceUnit: GetDistanceUnit,
    private val trackLocation: TrackLocation,
    private val saveSession: SaveSession,
    private val time: TimeSource,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(activity: ActivityType): ActiveSessionViewModel
    }

    private val unit = getDistanceUnit()
    private val recorder = SessionRecorder(activity)

    private val _state = MutableStateFlow(ActiveSessionUiState(activity = activity, unit = unit))
    val state: StateFlow<ActiveSessionUiState> = _state.asStateFlow()

    /** Set once the session is over; the summary decides between saving and discarding. */
    private val _report = MutableStateFlow<SessionReport?>(null)
    val report: StateFlow<SessionReport?> = _report.asStateFlow()

    private var phase = SessionPhase.Searching
    private var isLocked = false
    private var isShowingTooShort = false

    /** The latest fix, accepted or not; drives the signal chip. */
    private var lastSample: LocationSample? = null

    /** Ticked every second by [runClock]; all elapsed times are measured against it. */
    private var now = time.nowMillis()

    private var startDate: Long? = null
    private var endDate: Long? = null

    /** Moving time banked before the current recording stretch. */
    private var bankedMillis = 0L
    private var recordingSince: Long? = null
    private var pausedSince: Long? = null

    // MARK: Derived state

    private val signal: GPSSignal
        get() {
            val sample = lastSample ?: return GPSSignal.Searching
            if (now - sample.timestamp > STALE_FIX_MILLIS) return GPSSignal.Weak
            return GPSSignal.of(sample.horizontalAccuracy)
        }

    private val durationSeconds: Double
        get() {
            val since = recordingSince ?: return bankedMillis / 1_000.0
            return (bankedMillis + (now - since).coerceAtLeast(0)) / 1_000.0
        }

    private val pausedForSeconds: Double
        get() = pausedSince?.let { (now - it).coerceAtLeast(0) / 1_000.0 } ?: 0.0

    /** Hidden while paused or when the signal is weak, as the estimate would be misleading. */
    private val currentPace: Double?
        get() = if (phase == SessionPhase.Recording && signal == GPSSignal.Good) {
            RoutePace.recent(recorder.route)
        } else {
            null
        }

    private val averagePace: Double?
        get() = recorder.distanceMeters.takeIf { it > 0 }?.let { durationSeconds / it }

    private val canSave: Boolean
        get() = recorder.distanceMeters >= MIN_DISTANCE_METERS && durationSeconds >= MIN_DURATION_SECONDS

    // MARK: Lifetime

    init {
        // Owned by the view model, not the composition, so a rotation doesn't interrupt the recording.
        viewModelScope.launch { track() }
    }

    /** Consumes location fixes for as long as the calling coroutine runs. */
    private suspend fun track() {
        trackLocation().collect(::receive)
    }

    /**
     * Ticks the clock once a second for as long as the calling coroutine runs. Only the elapsed-time
     * labels need it, so the screen drives it; every action re-reads the clock for itself.
     */
    suspend fun runClock() {
        while (true) {
            tick()
            delay(1_000)
        }
    }

    override fun onCleared() {
        trackLocation.stop()
    }

    fun tick() {
        now = time.nowMillis()
        publish()
    }

    fun receive(sample: LocationSample) {
        // The provider can replay its last known fix first, which may be minutes old and miles away.
        if (time.nowMillis() - sample.timestamp > STALE_FIX_MILLIS) return
        lastSample = sample
        now = maxOf(now, sample.timestamp)
        when (phase) {
            SessionPhase.Searching ->
                if (GPSSignal.of(sample.horizontalAccuracy) == GPSSignal.Good) {
                    startRecording()
                    recorder.record(sample)
                }

            SessionPhase.Recording -> recorder.record(sample)
            SessionPhase.Paused, SessionPhase.Finished -> Unit
        }
        publish()
    }

    // MARK: Actions

    /** "Start Anyway" while still searching for GPS. */
    fun startRecording() {
        if (phase != SessionPhase.Searching) return
        now = time.nowMillis()
        startDate = now
        recordingSince = now
        phase = SessionPhase.Recording
        publish()
    }

    fun pause() {
        if (phase != SessionPhase.Recording) return
        now = time.nowMillis()
        bankDuration()
        pausedSince = now
        phase = SessionPhase.Paused
        publish()
    }

    fun resume() {
        if (phase != SessionPhase.Paused) return
        now = time.nowMillis()
        recordingSince = now
        pausedSince = null
        recorder.startNewSegment()
        phase = SessionPhase.Recording
        publish()
    }

    /** The pause button serves both directions. */
    fun togglePause() {
        if (phase == SessionPhase.Paused) resume() else pause()
    }

    fun lock() {
        isLocked = true
        publish()
    }

    fun unlock() {
        isLocked = false
        publish()
    }

    /** Hold-to-finish completed. Too-short sessions ask whether to keep going instead. */
    fun finish() {
        if (phase != SessionPhase.Recording && phase != SessionPhase.Paused) return
        now = time.nowMillis()
        if (!canSave) {
            isShowingTooShort = true
            publish()
            return
        }
        bankDuration()
        recorder.finish()
        pausedSince = null
        endDate = now
        phase = SessionPhase.Finished
        trackLocation.stop()
        publish()
    }

    /** "Keep Going" on the too-short sheet. */
    fun keepGoing() {
        isShowingTooShort = false
        resume()
        publish()
    }

    fun save() {
        val start = startDate ?: return
        val end = endDate ?: return
        if (phase != SessionPhase.Finished) return
        val session = SessionSummary(
            activity = activity,
            startDate = start,
            endDate = end,
            distanceMeters = recorder.distanceMeters,
            durationSeconds = durationSeconds,
            route = recorder.route,
        )
        val rawTrack = recorder.rawTrack.toList()
        viewModelScope.launch { saveSession(session, rawTrack) }
    }

    /** Cancel, discard, or leaving while searching: nothing is stored. */
    fun discard() {
        isShowingTooShort = false
        trackLocation.stop()
        publish()
    }

    private fun bankDuration() {
        recordingSince?.let { bankedMillis += (now - it).coerceAtLeast(0) }
        recordingSince = null
    }

    private fun publish() {
        _state.value = ActiveSessionUiState(
            activity = activity,
            phase = phase,
            signal = signal,
            isLocked = isLocked,
            isShowingTooShort = isShowingTooShort,
            distanceMeters = recorder.distanceMeters,
            durationSeconds = durationSeconds,
            pausedForSeconds = pausedForSeconds,
            currentPace = currentPace,
            averagePace = averagePace,
            route = recorder.route.toList(),
            unit = unit,
        )
        _report.value = if (phase == SessionPhase.Finished) {
            SessionReport(
                activity = activity,
                startDate = startDate ?: now,
                endDate = endDate ?: now,
                distanceMeters = recorder.distanceMeters,
                durationSeconds = durationSeconds,
                averagePace = averagePace,
                route = recorder.route.toList(),
                unit = unit,
            )
        } else {
            null
        }
    }

    companion object {
        /** Sessions shorter than either limit can't be saved. */
        const val MIN_DISTANCE_METERS = 100.0
        const val MIN_DURATION_SECONDS = 60.0

        /** With no fix for this long the signal counts as weak. */
        const val STALE_FIX_MILLIS = 10_000L
    }
}
