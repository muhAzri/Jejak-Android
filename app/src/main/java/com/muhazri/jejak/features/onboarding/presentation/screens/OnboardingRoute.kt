package com.muhazri.jejak.features.onboarding.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muhazri.jejak.core.system.rememberLocationPermissionRequest
import com.muhazri.jejak.features.onboarding.presentation.viewmodels.OnboardingViewModel
import kotlinx.coroutines.flow.collectLatest

/** Wires [OnboardingScreen] to its view model and the system permission dialog. */
@Composable
fun OnboardingRoute(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val isRequesting by viewModel.isRequestingPermission.collectAsStateWithLifecycle()
    val requestPermission = rememberLocationPermissionRequest { isGranted ->
        viewModel.onPermissionResult(isGranted)
        onFinish()
    }

    LaunchedEffect(viewModel) {
        viewModel.permissionRequests.collectLatest { requestPermission() }
    }

    OnboardingScreen(
        onFinish = onFinish,
        modifier = modifier,
        isRequestingPermission = isRequesting,
        onAllowLocation = viewModel::allowLocationTapped,
        onLater = {
            viewModel.laterTapped()
            onFinish()
        },
    )
}
