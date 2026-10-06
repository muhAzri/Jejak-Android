package com.muhazri.jejak.features.onboarding.domain.entities

enum class LocationPermission {
    NotDetermined,

    /** Granted with "Allow Once" in an earlier launch; the system will ask again at the next session. */
    AllowedOnce,
    Denied,
    WhenInUse,
    Always,
    ;

    val isGranted: Boolean get() = this == WhenInUse || this == Always
}
