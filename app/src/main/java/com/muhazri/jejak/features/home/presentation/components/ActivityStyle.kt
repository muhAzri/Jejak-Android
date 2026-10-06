package com.muhazri.jejak.features.home.presentation.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.muhazri.jejak.R
import com.muhazri.jejak.core.designsystem.HeroIcon
import com.muhazri.jejak.core.designsystem.JejakTheme
import com.muhazri.jejak.features.home.domain.entities.ActivityType

/** How an activity is labelled and colored across the app. */
@get:StringRes
val ActivityType.titleRes: Int
    get() = when (this) {
        ActivityType.Run -> R.string.activity_run
        ActivityType.Walk -> R.string.activity_walk
    }

@get:StringRes
val ActivityType.startTitleRes: Int
    get() = when (this) {
        ActivityType.Run -> R.string.activity_start_run
        ActivityType.Walk -> R.string.activity_start_walk
    }

@get:StringRes
val ActivityType.completedTitleRes: Int
    get() = when (this) {
        ActivityType.Run -> R.string.activity_run_complete
        ActivityType.Walk -> R.string.activity_walk_complete
    }

val ActivityType.icon: HeroIcon
    get() = when (this) {
        ActivityType.Run -> HeroIcon.Bolt
        ActivityType.Walk -> HeroIcon.GlobeAsiaAustralia
    }

/** Run = orange, Walk = teal. */
val ActivityType.tint: Color
    @Composable @ReadOnlyComposable get() = when (this) {
        ActivityType.Run -> JejakTheme.colors.run
        ActivityType.Walk -> JejakTheme.colors.walk
    }
