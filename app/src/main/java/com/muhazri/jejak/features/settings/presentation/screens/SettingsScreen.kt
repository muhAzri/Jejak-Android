package com.muhazri.jejak.features.settings.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhazri.jejak.BuildConfig
import com.muhazri.jejak.R
import com.muhazri.jejak.core.designsystem.HeroIcon
import com.muhazri.jejak.core.designsystem.HeroIconImage
import com.muhazri.jejak.core.designsystem.JejakFont
import com.muhazri.jejak.core.designsystem.JejakTheme
import com.muhazri.jejak.core.designsystem.RadioRow
import com.muhazri.jejak.core.designsystem.ScreenLayout
import com.muhazri.jejak.core.designsystem.pressable
import com.muhazri.jejak.core.designsystem.safeArea
import com.muhazri.jejak.features.onboarding.domain.entities.LocationPermission
import com.muhazri.jejak.features.settings.domain.entities.DistanceUnit
import java.util.Locale

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    locationPermission: LocationPermission = LocationPermission.WhenInUse,
    onOpenSystemSettings: () -> Unit = {},
) {
    val colors = JejakTheme.colors
    val insets = safeArea()
    var unit by rememberSaveable { mutableStateOf(DistanceUnit.Kilometers) }

    BoxWithConstraints(modifier.fillMaxSize().background(colors.surface)) {
        val layout = ScreenLayout.of(DpSize(maxWidth, maxHeight))
        val half = maxWidth / 2
        Column(Modifier.fillMaxSize().padding(bottom = insets.bottom)) {
            Column(
                Modifier
                    .padding(top = insets.top + layout.topMargin(insets.top))
                    .padding(start = insets.leading, end = insets.trailing),
            ) {
                Header(layout, onBack)
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
            }

            if (layout == ScreenLayout.Split) {
                // Each column is exactly half the screen; rows sit 32dp from the fold on both sides.
                Row(Modifier.fillMaxSize()) {
                    Column(
                        Modifier
                            .width(half)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(start = insets.leading, end = 16.dp),
                    ) {
                        UnitsSection(layout, unit) { unit = it }
                        SectionDivider(Modifier.padding(start = 16.dp))
                        GeneralSection(layout, locationPermission, onOpenSystemSettings)
                    }
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(start = 16.dp, end = insets.trailing),
                    ) {
                        AboutSection(layout)
                    }
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(start = insets.leading, end = insets.trailing),
                ) {
                    UnitsSection(layout, unit) { unit = it }
                    SectionDivider()
                    GeneralSection(layout, locationPermission, onOpenSystemSettings)
                    if (layout != ScreenLayout.Compact) SectionDivider()
                    AboutSection(layout)
                }
            }
        }
    }
}

/**
 * Back is a filled circle like Home's settings button. On the open fold the screen is held wide and the
 * top-left corner is far from the thumb, so the button and title grow.
 */
@Composable
private fun Header(layout: ScreenLayout, onBack: () -> Unit) {
    val colors = JejakTheme.colors
    val isSplit = layout == ScreenLayout.Split
    val buttonSize: Dp = if (isSplit) 52.dp else 44.dp
    val backLabel = stringResource(R.string.settings_back)
    val interaction = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = if (isSplit) 10.dp else 6.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(buttonSize)
                .background(colors.surfaceTint, CircleShape)
                .pressable(interaction, onClick = onBack)
                .semantics { contentDescription = backLabel },
        ) {
            HeroIconImage(HeroIcon.ChevronLeft, if (isSplit) 26.dp else 22.dp, colors.textPrimary)
        }
        Text(
            text = stringResource(R.string.settings_title),
            style = if (isSplit) JejakFont.display(24.sp) else JejakFont.h1,
            color = colors.textPrimary,
        )
    }
}

@Composable
private fun UnitsSection(layout: ScreenLayout, unit: DistanceUnit, onSelect: (DistanceUnit) -> Unit) {
    Column(Modifier.padding(bottom = if (layout == ScreenLayout.Regular) 8.dp else 4.dp)) {
        SectionTitle(stringResource(R.string.settings_units_section), layout)
        RadioRow(
            title = stringResource(R.string.settings_kilometers),
            isSelected = unit == DistanceUnit.Kilometers,
            onClick = { onSelect(DistanceUnit.Kilometers) },
        )
        RadioRow(
            title = stringResource(R.string.settings_miles),
            isSelected = unit == DistanceUnit.Miles,
            onClick = { onSelect(DistanceUnit.Miles) },
        )
    }
}

/**
 * Language and location are owned by the system, so both rows open the app's page in Settings
 * (location asks in-app first if it never has).
 */
@Composable
private fun GeneralSection(
    layout: ScreenLayout,
    permission: LocationPermission,
    onOpenSystemSettings: () -> Unit,
) {
    val language = remember { Locale.getDefault().displayLanguage }
    Column(Modifier.padding(bottom = if (layout == ScreenLayout.Regular) 8.dp else 4.dp)) {
        SectionTitle(stringResource(R.string.settings_general_section), layout)
        SettingsRow(
            icon = HeroIcon.Language,
            title = stringResource(R.string.settings_language),
            value = language,
            layout = layout,
            onClick = onOpenSystemSettings,
        )
        SettingsRow(
            icon = HeroIcon.MapPin,
            title = stringResource(R.string.settings_location_access),
            value = stringResource(permission.settingsLabelRes),
            layout = layout,
            onClick = onOpenSystemSettings,
        )
    }
}

@Composable
private fun AboutSection(layout: ScreenLayout) {
    val colors = JejakTheme.colors
    Column {
        // The closed fold's screen folds Version into General to keep everything on one screen.
        if (layout != ScreenLayout.Compact) {
            SectionTitle(stringResource(R.string.settings_about_section), layout)
        }
        SettingsRow(
            icon = HeroIcon.InformationCircle,
            title = stringResource(R.string.settings_version),
            value = BuildConfig.VERSION_NAME,
            layout = layout,
            onClick = null,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_about_note),
                style = JejakFont.p3,
                color = colors.textSecondary,
            )
            // Credit for the route thumbnails, which are too small to carry it themselves.
            Text(
                text = stringResource(R.string.settings_map_credit),
                style = JejakFont.p3,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String, layout: ScreenLayout) {
    Text(
        text = title,
        style = JejakFont.h2,
        color = JejakTheme.colors.textPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = if (layout == ScreenLayout.Regular) 16.dp else 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun SectionDivider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(5.dp).background(JejakTheme.colors.fillInput))
}

@Composable
private fun SettingsRow(
    icon: HeroIcon,
    title: String,
    value: String,
    layout: ScreenLayout,
    onClick: (() -> Unit)?,
) {
    val colors = JejakTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.pressable(interaction, onClick = onClick) else Modifier)
            .defaultMinSize(minHeight = if (layout == ScreenLayout.Regular) 56.dp else 52.dp)
            .padding(horizontal = 16.dp),
    ) {
        HeroIconImage(icon, 24.dp, colors.textPrimary)
        Text(title, style = JejakFont.p1, color = colors.textPrimary, modifier = Modifier.weight(1f))
        Text(value, style = JejakFont.p2, color = colors.textSecondary)
        if (onClick != null) {
            HeroIconImage(HeroIcon.ChevronRight, 20.dp, colors.textPrimary)
        }
    }
}

private val LocationPermission.settingsLabelRes: Int
    get() = when (this) {
        LocationPermission.WhenInUse -> R.string.permission_while_using
        LocationPermission.Always -> R.string.permission_always
        LocationPermission.AllowedOnce -> R.string.permission_once
        LocationPermission.Denied -> R.string.permission_off
        LocationPermission.NotDetermined -> R.string.permission_not_set
    }
