package com.sinc.mobile.app.features.createunidadproductiva.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sinc.mobile.app.ui.components.OsmdroidMapView
import com.sinc.mobile.domain.model.LocationError
import com.sinc.mobile.ui.theme.colorBotonSiguiente
import com.sinc.mobile.ui.theme.md_theme_light_primary
import org.osmdroid.util.GeoPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import java.util.Locale

// Define the ESRI World Imagery tile source
private val ESRI_WORLD_IMAGERY = object : XYTileSource(
    "Esri World Imagery",
    0, 19, 256, ".jpg",
    arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/")
) { }

@Composable
fun Step1Ubicacion(
    isMapVisible: Boolean,
    selectedLocation: GeoPoint?,
    locationError: LocationError?,
    onUseCurrentLocation: () -> Unit,
    onSearchOnMap: () -> Unit,
    onMapDismissed: () -> Unit,
    animateToLocation: GeoPoint?,
    onAnimationCompleted: () -> Unit,
    onConfirmLocation: (GeoPoint) -> Unit,
    isFetchingLocation: Boolean // New parameter
) {
    if (isMapVisible) {
        MapDialog(
            onDismiss = onMapDismissed,
            initialCenter = GeoPoint(-26.58116, -54.86023), // User-provided Misiones coordinates
            animateToLocation = animateToLocation,
            onAnimationCompleted = onAnimationCompleted,
            onConfirmLocation = onConfirmLocation,
            isFetchingLocation = isFetchingLocation // Pass new parameter
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¿Cómo desea registrar la ubicación del campo?",
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.DarkGray),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        ActionButton(
            text = "Usar mi ubicación actual",
            legend = "La app tomará las coordenadas del GPS",
            onClick = onUseCurrentLocation
        )

        Spacer(modifier = Modifier.height(24.dp))

        ActionButton(
            text = "Buscar en el mapa",
            legend = "Mover un marcador a la ubicación deseada",
            onClick = onSearchOnMap
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (locationError != null) {
            Text(
                text = locationError.toUserFriendlyMessage(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

private fun LocationError.toUserFriendlyMessage(): String {
    return when (this) {
        LocationError.NoPermission -> "No se pudo acceder a la ubicación porque no se concedieron los permisos."
        LocationError.PermissionDenied -> "Permiso de ubicación denegado. Por favor, actívelo en los ajustes de la aplicación."
        LocationError.GpsDisabled -> "El GPS está desactivado. Por favor, actívelo para obtener la ubicación."
        LocationError.LocationNotFound -> "No se pudo determinar la ubicación. Intente de nuevo en un área con mejor señal."
        LocationError.UnknownError -> "Ocurrió un error desconocido al obtener la ubicación."
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapDialog(
    onDismiss: () -> Unit,
    initialCenter: GeoPoint,
    animateToLocation: GeoPoint?,
    onAnimationCompleted: () -> Unit,
    onConfirmLocation: (GeoPoint) -> Unit,
    isFetchingLocation: Boolean // New parameter
) {
    var mapCenter by remember { mutableStateOf(initialCenter) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Seleccione la ubicación") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar mapa")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            bottomBar = {
                // New Bottom Sheet style
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Ubicación",
                            style = MaterialTheme.typography.titleMedium,
                            color = colorBotonSiguiente, // Green color
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = buildAnnotatedString {
                                append("Presione el botón ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Guardar Ubicación")
                                }
                                append(" para confirmar o ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("deslice el mapa")
                                }
                                append(" y utilice los controles de zoom para precisar mejor.")
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        )
                        Button(
                            onClick = { onConfirmLocation(mapCenter) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = colorBotonSiguiente),
                            enabled = !isFetchingLocation
                        ) {
                            Text("Guardar Ubicación")
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                OsmdroidMapView(
                    modifier = Modifier.fillMaxSize(),
                    initialCenter = initialCenter,
                    onMapReady = {},
                    animateToLocation = animateToLocation,
                    onAnimationCompleted = onAnimationCompleted,
                    onMapMove = { newCenter ->
                        mapCenter = newCenter
                    },
                    tileSource = TileSourceFactory.MAPNIK,
                    initialZoom = 18.0
                )

                // Central Marker Icon
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Marcador",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp),
                    tint = md_theme_light_primary
                )

                // New Loading Card
                if (isFetchingLocation) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(color = md_theme_light_primary)
                                Text("Cargando...")
                            }
                        }
                    }
                }
            }
        }
    }
}