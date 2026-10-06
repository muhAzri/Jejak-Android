package com.muhazri.jejak.core.system

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Opens this app's page in system settings, where location and language live. */
@Composable
fun rememberOpenAppSettings(): () -> Unit {
    val context = LocalContext.current
    return remember(context) {
        {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}

/**
 * The system location dialog. Both the precise and the coarse permission are asked for together, so
 * a user who picks "Approximate" still gets a (rougher) route rather than nothing at all.
 *
 * On Android 13+ the ongoing-session notification also needs permission to be visible, so it is
 * asked for in the same dialog. A refusal only hides the notification; recording still works.
 *
 * [onResult] receives whether either location permission was granted.
 */
@Composable
fun rememberLocationPermissionRequest(onResult: (Boolean) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        // Only location decides whether a session can record; the notification is a nicety.
        onResult(LOCATION_PERMISSIONS.any { grants[it] == true })
    }
    return { launcher.launch(requestedPermissions()) }
}

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

private fun requestedPermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        LOCATION_PERMISSIONS + Manifest.permission.POST_NOTIFICATIONS
    } else {
        LOCATION_PERMISSIONS
    }
