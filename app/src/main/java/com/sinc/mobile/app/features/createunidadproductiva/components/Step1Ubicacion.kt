package com.sinc.mobile.app.features.createunidadproductiva.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sinc.mobile.app.ui.components.OsmdroidMapView
import com.sinc.mobile.domain.model.DomainGeoPoint
import com.sinc.mobile.domain.model.LocationError
import com.sinc.mobile.domain.model.Municipio
import com.sinc.mobile.ui.theme.colorBotonSiguiente
import com.sinc.mobile.ui.theme.md_theme_light_primary
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing // Added for contentWindowInsets

enum class MapMode {
    CURRENT_LOCATION,
    SEARCH_ON_MAP
}

@Composable
fun Step1Ubicacion(
    isMapVisible: Boolean,
    mapMode: MapMode,
    selectedLocation: DomainGeoPoint?,
    locationError: LocationError?,
    onUseCurrentLocation: () -> Unit,
    onSearchOnMap: () -> Unit,
    onMapDismissed: () -> Unit,
    animateToLocation: DomainGeoPoint?,
    onAnimationCompleted: () -> Unit,
    onConfirmLocation: (GeoPoint) -> Unit,
    isFetchingLocation: Boolean,
    municipios: List<Municipio>,
    selectedMunicipio: Municipio?,
    onMunicipioSelected: (Municipio) -> Unit
) {
    if (isMapVisible) {
        MapDialog(
            mapMode = mapMode,
            onDismiss = onMapDismissed,
            initialCenter = GeoPoint(-26.58116, -54.86023), // Misiones coordinates
            animateToLocation = animateToLocation,
            onAnimationCompleted = onAnimationCompleted,
            onConfirmLocation = onConfirmLocation,
            isFetchingLocation = isFetchingLocation,
            municipios = municipios,
            selectedMunicipio = selectedMunicipio,
            onMunicipioSelected = onMunicipioSelected
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
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
    mapMode: MapMode,
    onDismiss: () -> Unit,
    initialCenter: GeoPoint,
    animateToLocation: DomainGeoPoint?,
    onAnimationCompleted: () -> Unit,
    onConfirmLocation: (GeoPoint) -> Unit,
    isFetchingLocation: Boolean,
    municipios: List<Municipio>,
    selectedMunicipio: Municipio?,
    onMunicipioSelected: (Municipio) -> Unit
) {
    var mapCenter by remember { mutableStateOf(initialCenter) }
    val bottomSheetState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()
    var isSearchActive by remember { mutableStateOf(false) }

    LaunchedEffect(selectedMunicipio) {
        if (selectedMunicipio != null) {
            isSearchActive = false // Collapse when a selection is made
            bottomSheetState.bottomSheetState.partialExpand()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
            BottomSheetScaffold(
                modifier = Modifier.imePadding(),
                scaffoldState = bottomSheetState,
                sheetPeekHeight = if (mapMode == MapMode.SEARCH_ON_MAP) 40.dp else 0.dp,
                sheetContainerColor = Color.White, // Set background color to white
                sheetDragHandle = null, // Disable the default drag handle
                sheetContent = {
                    if (mapMode == MapMode.SEARCH_ON_MAP) {
                        SearchableSheetContent(
                            modifier = if (isSearchActive) Modifier.fillMaxHeight() else Modifier.fillMaxHeight(0.5f),
                            municipios = municipios,
                            selectedMunicipio = selectedMunicipio,
                            onMunicipioSelected = onMunicipioSelected,
                            onSearchFocus = {
                                isSearchActive = true
                                scope.launch {
                                    bottomSheetState.bottomSheetState.expand()
                                }
                            }
                        )
                    } else {
                        // Empty content for other modes
                        Box(modifier = Modifier.height(1.dp))
                    }
                }
            ) {
                val peekHeight = if (mapMode == MapMode.SEARCH_ON_MAP) 40.dp else 0.dp
                Box(modifier = Modifier.fillMaxSize()) {
                    OsmdroidMapView(
                        modifier = Modifier.fillMaxSize(),
                        initialCenter = initialCenter,
                        onMapReady = {},
                        animateToLocation = animateToLocation?.let { GeoPoint(it.latitude, it.longitude) },
                        jumpToLocation = null,
                        onAnimationCompleted = onAnimationCompleted,
                        onMapMove = { newCenter ->
                            mapCenter = newCenter
                        },
                        tileSource = TileSourceFactory.MAPNIK,
                        initialZoom = 9.0,
                        selectedMunicipio = selectedMunicipio
                    )

                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(50))
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar mapa",
                            tint = md_theme_light_primary
                        )
                    }

                    // Save Location Button
                    Button(
                        onClick = { onConfirmLocation(mapCenter) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = peekHeight + 16.dp) // Dynamic padding
                            .fillMaxWidth(0.8f),
                        colors = ButtonDefaults.buttonColors(containerColor = colorBotonSiguiente),
                        enabled = !isFetchingLocation
                    ) {
                        Text("Guardar Ubicación")
                    }

                    // Central Marker
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Marcador",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(40.dp),
                        tint = md_theme_light_primary
                    )

                    // Loading indicator
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchableSheetContent(
    modifier: Modifier = Modifier,
    municipios: List<Municipio>,
    selectedMunicipio: Municipio?,
    onMunicipioSelected: (Municipio) -> Unit,
    onSearchFocus: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(selectedMunicipio) {
        searchText = selectedMunicipio?.nombre ?: ""
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Custom Drag Handle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Deslice para buscar municipios",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp)) // Added Spacer

        // Content for the expanded state
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Escriba el nombre del municipio") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    if (it.isFocused) {
                        onSearchFocus()
                    }
                },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = Color.Gray
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorBotonSiguiente,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = colorBotonSiguiente
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        val filteredMunicipios = if (searchText.isNotEmpty()) {
            municipios.filter {
                it.nombre.contains(searchText, ignoreCase = true)
            }
        } else {
            emptyList()
        }

        LazyColumn(modifier = Modifier.weight(1f)) { // Use weight to fill remaining space
            items(filteredMunicipios) { municipio ->
                DropdownMenuItem(
                    text = { Text(municipio.nombre) },
                    onClick = { onMunicipioSelected(municipio) }
                )
                HorizontalDivider()
            }
        }
    }
}
