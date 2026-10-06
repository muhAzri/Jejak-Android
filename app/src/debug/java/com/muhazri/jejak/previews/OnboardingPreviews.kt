package com.muhazri.jejak.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.muhazri.jejak.core.designsystem.JejakTheme
import com.muhazri.jejak.features.onboarding.presentation.screens.OnboardingScreen

@Preview(showBackground = true)
@Composable
private fun OnboardingPreview() = JejakTheme {
    OnboardingScreen(onFinish = {})
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun OnboardingDarkPreview() = JejakTheme(darkTheme = true) {
    OnboardingScreen(onFinish = {})
}

@Preview(name = "Fold closed", widthDp = 400, heightDp = 566, showBackground = true)
@Composable
private fun OnboardingFoldClosedPreview() = JejakTheme {
    OnboardingScreen(onFinish = {})
}

@Preview(name = "Fold open", widthDp = 800, heightDp = 566, showBackground = true)
@Composable
private fun OnboardingFoldOpenPreview() = JejakTheme {
    OnboardingScreen(onFinish = {})
}
