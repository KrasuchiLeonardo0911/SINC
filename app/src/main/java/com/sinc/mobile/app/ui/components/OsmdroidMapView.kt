package com.sinc.mobile.app.ui.components

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
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

import org.osmdroid.tileprovider.tilesource.ITileSource

@Composable
fun OsmdroidMapView(
    modifier: Modifier = Modifier,
    initialCenter: GeoPoint,
    onMapReady: (MapView) -> Unit,
    animateToLocation: GeoPoint?,
    onAnimationCompleted: () -> Unit,
    onMapMove: ((GeoPoint) -> Unit)? = null,
    tileSource: ITileSource = TileSourceFactory.MAPNIK,
    initialZoom: Double = 12.0
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context)
    }

    LaunchedEffect(animateToLocation) {
        if (animateToLocation != null) {
            mapView.controller.animateTo(animateToLocation, 17.0, 1500L)
            mapView.invalidate() // Force a redraw after animation
            delay(100)
            onAnimationCompleted()
        }
    }

    AndroidView(
        factory = {
            mapView.apply {
                setTileSource(tileSource)
                setMultiTouchControls(true)
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
