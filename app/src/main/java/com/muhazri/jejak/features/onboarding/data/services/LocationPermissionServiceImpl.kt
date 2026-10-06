package com.muhazri.jejak.features.onboarding.data.services

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.muhazri.jejak.features.onboarding.domain.entities.LocationPermission
import com.muhazri.jejak.features.onboarding.domain.repositories.LocationPermissionService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Reads the location grant from the platform.
 *
 * Android has no API for "Only this time" either: the grant disappears when the process ends and the
 * permission reads as never-granted again. A recorded grant that is gone is therefore reported as
 * [LocationPermission.AllowedOnce] — which is also the right notice when the grant was taken away in
 * Settings, since both are fixed the same way.
 */
@Singleton
class LocationPermissionServiceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val preferences: SharedPreferences,
) : LocationPermissionService {

    private val state = MutableStateFlow(read())

    override fun current(): LocationPermission = state.value

    override fun updates(): Flow<LocationPermission> = state.asStateFlow()

    override fun refresh() {
        state.value = read()
    }

    override fun onRequestResult(isGranted: Boolean) {
        preferences.edit {
            putBoolean(WAS_REQUESTED_KEY, true)
            putBoolean(WAS_GRANTED_KEY, isGranted)
        }
        refresh()
    }

    private fun read(): LocationPermission {
        val isPrecise = isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        val isCoarse = isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        val isBackground = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            isGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

        return when {
            isPrecise || isCoarse -> {
                preferences.edit { putBoolean(WAS_GRANTED_KEY, true) }
                if (isBackground) LocationPermission.Always else LocationPermission.WhenInUse
            }

            preferences.getBoolean(WAS_GRANTED_KEY, false) -> LocationPermission.AllowedOnce
            preferences.getBoolean(WAS_REQUESTED_KEY, false) -> LocationPermission.Denied
            else -> LocationPermission.NotDetermined
        }
    }

    private fun isGranted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private companion object {
        const val WAS_GRANTED_KEY = "location.wasGranted"
        const val WAS_REQUESTED_KEY = "location.wasRequested"
    }
}
