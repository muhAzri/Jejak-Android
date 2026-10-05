package com.muhazri.jejak.app.navigation

import kotlinx.serialization.Serializable

/** Type-safe navigation destinations; add arguments as constructor properties. */
@Serializable
sealed interface Route {

    @Serializable
    data object Counter : Route
}
