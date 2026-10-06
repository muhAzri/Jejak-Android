package com.muhazri.jejak.app.navigation

import com.muhazri.jejak.features.home.domain.entities.ActivityType
import kotlinx.serialization.Serializable

/** Type-safe navigation destinations; add arguments as constructor properties. */
@Serializable
sealed interface Route {

    @Serializable
    data object Onboarding : Route

    @Serializable
    data object Home : Route

    @Serializable
    data object Settings : Route

    /** The live recording screen for one activity. */
    @Serializable
    data class ActiveSession(val activity: ActivityType) : Route

    /** A saved session, opened from Home's "Last Session". */
    @Serializable
    data object SessionDetail : Route
}
