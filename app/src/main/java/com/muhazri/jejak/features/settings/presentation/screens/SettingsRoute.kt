package com.muhazri.jejak.features.settings.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muhazri.jejak.core.system.rememberLocationPermissionRequest
import com.muhazri.jejak.core.system.rememberOpenAppSettings
import com.muhazri.jejak.features.settings.presentation.viewmodels.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest

/** Wires [SettingsScreen] to its view model. */
@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
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
        viewModel.permissionRequests.collectLatest { requestPermission() }
    }

    SettingsScreen(
        onBack = onBack,
        modifier = modifier,
        locationPermission = state.locationPermission,
        unit = state.unit,
        onSelectUnit = viewModel::select,
        onOpenSystemSettings = openAppSettings,
        onRequestPermission = viewModel::requestPermission,
    )
}
