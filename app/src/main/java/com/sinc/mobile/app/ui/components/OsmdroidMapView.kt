package com.sinc.mobile.app.ui.components

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent

import org.osmdroid.views.overlay.Polygon
import android.graphics.Color as AndroidColor
import org.osmdroid.tileprovider.tilesource.ITileSource

@Composable
fun OsmdroidMapView(
    modifier: Modifier = Modifier,
    initialCenter: GeoPoint,
    onMapReady: (MapView) -> Unit,
    animateToLocation: GeoPoint?,
    jumpToLocation: GeoPoint?,
    onAnimationCompleted: () -> Unit,
    onMapMove: ((GeoPoint) -> Unit)? = null,
    tileSource: ITileSource = TileSourceFactory.MAPNIK,
    initialZoom: Double = 12.0,
    selectedMunicipio: com.sinc.mobile.domain.model.Municipio? // New parameter
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context)
    }

    LaunchedEffect(jumpToLocation) {
        if (jumpToLocation != null) {
            mapView.controller.setZoom(15.0)
            mapView.controller.setCenter(jumpToLocation)
            mapView.invalidate()
        }
    }

    LaunchedEffect(animateToLocation) {
        if (animateToLocation != null) {
            // This effect is for animating to a single point (e.g., user's current location)
            // It should only run when polygons are not being handled.
            if (selectedMunicipio == null) {
                // 1. Jump to the intermediate zoom level without animation
                mapView.controller.setZoom(15.0)
                mapView.controller.setCenter(animateToLocation)
                mapView.invalidate()

                delay(200) // Brief delay to allow rendering

                // 2. Animate to the final zoom level
                mapView.controller.animateTo(animateToLocation, 17.0, 1500L)
                mapView.invalidate() // Force a redraw after animation
                delay(100)
                onAnimationCompleted()
            }
        }
    }

    LaunchedEffect(selectedMunicipio) {
        Log.d("OsmdroidMapView", "Municipio effect triggered. New municipio: ${selectedMunicipio?.nombre}")
        mapView.overlays.clear() // Clear existing overlays
        val polygonPoints = selectedMunicipio?.poligono?.map { GeoPoint(it.latitude, it.longitude) }

        if (!polygonPoints.isNullOrEmpty()) {
            val polygonOverlay = Polygon().apply {
                points = polygonPoints
                fillColor = AndroidColor.argb(75, 100, 100, 200) // Semi-transparent blue
                strokeColor = AndroidColor.rgb(0, 0, 150)
                strokeWidth = 3f
            }
            mapView.overlays.add(polygonOverlay)
            Log.d("OsmdroidMapView", "Polygon added to map. Bounds: ${polygonOverlay.bounds}")

            // After a short delay, zoom to the bounding box of the polygon
            delay(200)
            mapView.zoomToBoundingBox(polygonOverlay.bounds, true, 100) // true for animated, 100 for padding
            onAnimationCompleted()
        }
        mapView.invalidate()
    }

    AndroidView(
        factory = {
            mapView.apply {
                setTileSource(tileSource)
                setMultiTouchControls(true)
                setBuiltInZoomControls(false) // Add this line
                controller.setZoom(initialZoom)
                controller.setCenter(initialCenter)
                onMapReady(this)

                if (onMapMove != null) {
                    addMapListener(object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            onMapMove(mapCenter as GeoPoint)
                            return true
                        }

                        override fun onZoom(event: ZoomEvent?): Boolean {
                            onMapMove(mapCenter as GeoPoint)
                            return true
                        }
                    })
                }
            }
        },
        modifier = modifier
    )
}
