package com.muhazri.jejak.features.home.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muhazri.jejak.core.system.rememberLocationPermissionRequest
import com.muhazri.jejak.core.system.rememberOpenAppSettings
import com.muhazri.jejak.features.home.domain.entities.ActivityType
import com.muhazri.jejak.features.home.presentation.viewmodels.HomeEvent
import com.muhazri.jejak.features.home.presentation.viewmodels.HomeViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Wires [HomeScreen] to its view model. Everything Home shows is re-read on resume, which is also
 * how it notices a permission changed in system settings or a unit changed in Settings.
 */
@Composable
fun HomeRoute(
    onOpenSettings: () -> Unit,
    onStartActivity: (ActivityType) -> Unit,
    onOpenSessionDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val openAppSettings = rememberOpenAppSettings()
    val requestPermission = rememberLocationPermissionRequest(viewModel::onPermissionResult)

    LifecycleResumeEffect(viewModel) {
        viewModel.refresh()
        onPauseOrDispose {}
    }

    LaunchedEffect(viewModel) { viewModel.observePermission() }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                HomeEvent.RequestLocationPermission -> requestPermission()
                is HomeEvent.StartActivity -> onStartActivity(event.activity)
            }
        }
    }

    HomeScreen(
        onOpenSettings = onOpenSettings,
        onStartActivity = viewModel::start,
        onOpenLastSession = { onOpenSessionDetail(it.id) },
        modifier = modifier,
        permission = state.permission,
        lastSession = state.lastSession,
        unit = state.unit,
        isRequestingPermission = state.isRequestingPermission,
        onOpenSystemSettings = openAppSettings,
        onRequestPermission = viewModel::requestPermission,
    )
}
