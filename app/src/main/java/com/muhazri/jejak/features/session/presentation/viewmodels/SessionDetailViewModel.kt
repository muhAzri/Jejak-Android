package com.muhazri.jejak.features.session.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhazri.jejak.features.home.domain.entities.SessionSummary
import com.muhazri.jejak.features.home.domain.usecases.DeleteSession
import com.muhazri.jejak.features.home.domain.usecases.GetSession
import com.muhazri.jejak.features.settings.domain.entities.DistanceUnit
import com.muhazri.jejak.features.settings.domain.usecases.GetDistanceUnit
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionDetailUiState(
    val session: SessionSummary? = null,
    val unit: DistanceUnit = DistanceUnit.Kilometers,
    /** True once the session is gone, so the screen can close itself. */
    val isDeleted: Boolean = false,
)

/** A saved session, loaded by the id the navigator carried over from Home. */
@HiltViewModel(assistedFactory = SessionDetailViewModel.Factory::class)
class SessionDetailViewModel @AssistedInject constructor(
    @Assisted private val sessionId: String,
    getDistanceUnit: GetDistanceUnit,
    private val getSession: GetSession,
    private val deleteSession: DeleteSession,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(sessionId: String): SessionDetailViewModel
    }

    private val _state = MutableStateFlow(SessionDetailUiState(unit = getDistanceUnit()))
    val state: StateFlow<SessionDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val session = getSession(sessionId)
            _state.update { it.copy(session = session) }
        }
    }

    fun delete() {
        viewModelScope.launch {
            deleteSession(sessionId)
            _state.update { it.copy(isDeleted = true) }
        }
    }
}
