package com.muhazri.jejak.core.map

import android.content.Context
import android.view.GestureDetector
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.MotionEvent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView

/**
 * An osmdroid [MapView] set up the way every Jejak map wants it: OpenStreetMap raster tiles,
 * desaturated (and inverted in dark) so they read as the muted backdrop the design calls for,
 * with no controls of its own.
 *
 * [overlays] runs on every recomposition and owns the map's contents; it should clear and rebuild
 * `mapView.overlays`, then frame the map.
 */
@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    interactive: Boolean = false,
    onTap: (() -> Unit)? = null,
    overlays: (MapView) -> Unit,
) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember(interactive) {
        (if (interactive) MapView(context) else StaticMapView(context)).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setTilesScaledToDpi(true)
            setMultiTouchControls(interactive)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            // One world, so a route near the antimeridian can't be drawn twice.
            setHorizontalMapRepetitionEnabled(false)
            setVerticalMapRepetitionEnabled(false)
        }
    }

    // osmdroid runs tile threads of its own; they have to follow the host's lifecycle.
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = {
            (it as? StaticMapView)?.onTap = onTap
            it.overlayManager.tilesOverlay.setColorFilter(tileFilter(dark))
            overlays(it)
            it.invalidate()
        },
    )
}

/**
 * A map that is a picture rather than a control. It refuses every touch outright, so a tap on a
 * route thumbnail still opens the session behind it and a drag still scrolls the page — which a
 * MapView that merely swallowed the gesture would block.
 */
private class StaticMapView(context: Context) : MapView(context) {

    /** Set when something behind the map is tappable, e.g. the row a route thumbnail sits in. */
    var onTap: (() -> Unit)? = null

    private val taps = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(event: MotionEvent) = true

            override fun onSingleTapUp(event: MotionEvent): Boolean {
                onTap?.invoke()
                return true
            }
        },
    )

    /**
     * Never pans or zooms — `super.onTouchEvent` is deliberately not called. A tap goes to [onTap];
     * with nothing to tap the map declines the gesture outright so a drag scrolls the page.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean =
        onTap != null && taps.onTouchEvent(event)
}

/**
 * Frames [points] with [paddingPx] of breathing room. A single point (or a route that never moved)
 * has no box to fit, so it just gets centred at a sensible street-level zoom.
 */
fun MapView.frame(points: List<GeoPoint>, paddingPx: Int) {
    if (points.isEmpty()) return
    val apply = {
        val box = BoundingBox.fromGeoPointsSafe(points)
        if (box.latitudeSpan <= MIN_SPAN || box.longitudeSpanWithDateLine <= MIN_SPAN) {
            controller.setZoom(DEFAULT_ZOOM)
            controller.setCenter(points.first())
        } else {
            zoomToBoundingBox(box, false, paddingPx)
        }
    }
    // zoomToBoundingBox needs a measured view; before the first layout there is nothing to fit to.
    if (width == 0 || height == 0) {
        addOnFirstLayoutListener { _, _, _, _, _ -> apply() }
    } else {
        apply()
    }
}

private const val MIN_SPAN = 1e-7
private const val DEFAULT_ZOOM = 16.0

/** How much colour the tiles keep; the design wants them well behind the route. */
private const val TILE_SATURATION = 0.35f

private val INVERT = floatArrayOf(
    -1f, 0f, 0f, 0f, 255f,
    0f, -1f, 0f, 0f, 255f,
    0f, 0f, -1f, 0f, 255f,
    0f, 0f, 0f, 1f, 0f,
)

/**
 * Hue rotated half a turn. Inverting alone also flips the hues, which turns OSM's warm land and
 * blue water into blue land and orange water; rotating them back restores a believable dark map.
 */
private val HUE_ROTATE_180 = floatArrayOf(
    -0.574f, 1.430f, 0.144f, 0f, 0f,
    0.426f, 0.430f, 0.144f, 0f, 0f,
    0.426f, 1.430f, -0.856f, 0f, 0f,
    0f, 0f, 0f, 1f, 0f,
)

private fun tileFilter(dark: Boolean): ColorMatrixColorFilter {
    val matrix = ColorMatrix().apply { setSaturation(TILE_SATURATION) }
    if (dark) {
        matrix.postConcat(ColorMatrix(INVERT))
        matrix.postConcat(ColorMatrix(HUE_ROTATE_180))
    }
    return ColorMatrixColorFilter(matrix)
}
