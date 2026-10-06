package com.muhazri.jejak.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.muhazri.jejak.core.designsystem.JejakTheme
import com.muhazri.jejak.features.home.presentation.screens.HomeScreen
import com.muhazri.jejak.features.onboarding.domain.entities.LocationPermission

@Preview(name = "Empty", showBackground = true)
@Composable
private fun HomeEmptyPreview() = JejakTheme {
    HomeScreen(onOpenSettings = {}, onStartActivity = {}, onOpenLastSession = {})
}

@Preview(name = "Last session", showBackground = true)
@Composable
private fun HomeLastSessionPreview() = JejakTheme {
    HomeScreen(
        onOpenSettings = {},
        onStartActivity = {},
        onOpenLastSession = {},
        lastSession = SampleData.session,
    )
}

@Preview(name = "Location not requested", showBackground = true)
@Composable
private fun HomeNotRequestedPreview() = JejakTheme {
    HomeScreen(
        onOpenSettings = {},
        onStartActivity = {},
        onOpenLastSession = {},
        permission = LocationPermission.NotDetermined,
    )
}

@Preview(name = "Location denied", showBackground = true)
@Composable
private fun HomeDeniedPreview() = JejakTheme {
    HomeScreen(
        onOpenSettings = {},
        onStartActivity = {},
        onOpenLastSession = {},
        permission = LocationPermission.Denied,
    )
}

@Preview(name = "Allowed once · dark", showBackground = true)
@Composable
private fun HomeAllowedOncePreview() = JejakTheme(darkTheme = true) {
    HomeScreen(
        onOpenSettings = {},
        onStartActivity = {},
        onOpenLastSession = {},
        permission = LocationPermission.AllowedOnce,
        lastSession = SampleData.session,
    )
}

@Preview(name = "Fold closed", widthDp = 400, heightDp = 566, showBackground = true)
@Composable
private fun HomeFoldClosedPreview() = JejakTheme {
    HomeScreen(
        onOpenSettings = {},
        onStartActivity = {},
        onOpenLastSession = {},
        lastSession = SampleData.session,
    )
}

@Preview(name = "Fold open", widthDp = 800, heightDp = 566, showBackground = true)
@Composable
private fun HomeFoldOpenPreview() = JejakTheme {
    HomeScreen(
        onOpenSettings = {},
        onStartActivity = {},
        onOpenLastSession = {},
        permission = LocationPermission.AllowedOnce,
        lastSession = SampleData.session,
    )
}
