package com.muhazri.jejak.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.muhazri.jejak.core.designsystem.JejakTheme
import com.muhazri.jejak.features.settings.presentation.screens.SettingsScreen

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() = JejakTheme {
    SettingsScreen(onBack = {})
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun SettingsDarkPreview() = JejakTheme(darkTheme = true) {
    SettingsScreen(onBack = {})
}

@Preview(name = "Fold closed", widthDp = 400, heightDp = 566, showBackground = true)
@Composable
private fun SettingsFoldClosedPreview() = JejakTheme {
    SettingsScreen(onBack = {})
}

@Preview(name = "Fold open", widthDp = 800, heightDp = 566, showBackground = true)
@Composable
private fun SettingsFoldOpenPreview() = JejakTheme {
    SettingsScreen(onBack = {})
}
