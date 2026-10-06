package com.muhazri.jejak.features.session.data.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.muhazri.jejak.features.session.domain.entities.LocationSample
import com.muhazri.jejak.features.session.domain.repositories.LocationTrackingService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

/**
 * Feeds raw fixes from the platform's own location provider. No Google Play dependency, in keeping
 * with the OpenStreetMap tiles: Jejak needs nothing but the device.
 *
 * A session has to keep recording while the screen is off, which on Android means a foreground
 * service — [SessionTrackingService] runs for as long as updates do.
 */
@Singleton
class LocationTrackingServiceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : LocationTrackingService {

    private val manager = context.getSystemService<LocationManager>()

    /** The live stream, so [stop] can end it from outside the collector. */
    private var producer: ProducerScope<LocationSample>? = null

    override fun start(): Flow<LocationSample> = callbackFlow {
        // A newer start() owns the updates now.
        producer?.close()
        producer = this

        val listener = LocationListener { location -> trySend(location.toSample()) }
        val provider = provider()
        if (manager == null || provider == null || !hasPermission()) {
            // Nothing to listen to: the session stays on "Searching" and the signal chip says so.
            Log.w(TAG, "Location updates unavailable (provider=$provider, permission=${hasPermission()})")
            awaitClose {}
            return@callbackFlow
        }

        SessionTrackingService.start(context)
        try {
            manager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
        } catch (error: SecurityException) {
            Log.e(TAG, "Location permission revoked mid-session", error)
            close()
        }

        awaitClose {
            manager.removeUpdates(listener)
            SessionTrackingService.stop(context)
            if (producer === this) producer = null
        }
    }.buffer(BUFFER, BufferOverflow.DROP_OLDEST)

    override fun stop() {
        producer?.close()
        producer = null
    }

    /**
     * GPS first, which is what a fitness tracker needs — the counterpart to iOS's
     * `kCLLocationAccuracyBest` with a `.fitness` activity type.
     *
     * The fused provider is deliberately *not* preferred: it is a power-balanced blend that often
     * answers with ~100 m network fixes, and the pipeline drops anything worse than
     * [com.muhazri.jejak.features.session.domain.entities.SessionRecorder.MAX_USABLE_ACCURACY] (65 m).
     * It only stands in for a device with no GPS at all.
     */
    private fun provider(): String? {
        val manager = manager ?: return null
        val preferred = buildList {
            add(LocationManager.GPS_PROVIDER)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(LocationManager.FUSED_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
        }
        return preferred.firstOrNull { manager.allProviders.contains(it) }
    }

    private fun hasPermission() =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    private companion object {
        const val TAG = "LocationTracking"
        const val BUFFER = 32
    }
}

/** Everything the device didn't measure is reported as negative, the same convention Core Location uses. */
internal fun Location.toSample(): LocationSample {
    val hasExtras = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    return LocationSample(
        latitude = latitude,
        longitude = longitude,
        horizontalAccuracy = if (hasAccuracy()) accuracy.toDouble() else -1.0,
        timestamp = time,
        speed = if (hasSpeed()) speed.toDouble() else -1.0,
        speedAccuracy = if (hasExtras && hasSpeedAccuracy()) {
            speedAccuracyMetersPerSecond.toDouble()
        } else {
            -1.0
        },
        course = if (hasBearing()) bearing.toDouble() else -1.0,
        courseAccuracy = if (hasExtras && hasBearingAccuracy()) bearingAccuracyDegrees.toDouble() else -1.0,
        altitude = if (hasAltitude()) altitude else 0.0,
        verticalAccuracy = if (hasExtras && hasAltitude() && hasVerticalAccuracy()) {
            verticalAccuracyMeters.toDouble()
        } else {
            -1.0
        },
    )
}
