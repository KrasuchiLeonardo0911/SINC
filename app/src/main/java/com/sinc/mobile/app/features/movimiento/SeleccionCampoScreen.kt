package com.sinc.mobile.app.features.movimiento

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sinc.mobile.app.features.movimiento.components.CampoListItem
import com.sinc.mobile.app.navigation.Routes
import com.sinc.mobile.app.ui.components.MinimalHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionCampoScreen(
    modifier: Modifier = Modifier,
    viewModel: MovimientoStepperViewModel = hiltViewModel(),
    navController: NavController,
    onBack: () -> Unit,
    mainScaffoldBottomPadding: Dp,
) {
    // Observe the UI state from MovimientoStepperViewModel
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MinimalHeader(
                title = "Seleccionar Campo",
                onBackPress = onBack,
                modifier = Modifier
                    .statusBarsPadding()
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = mainScaffoldBottomPadding),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp), // Add horizontal padding to the LazyColumn
                    contentPadding = PaddingValues(top = 16.dp) // Add top padding for content separation
                ) {
                    item {
                        // Header
                        Column {
                            Text(
                                text = "Seleccione el campo",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary // Use primary color for green
                                ),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "¿A qué campo corresponde el registro de stock?",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant // Use onSurfaceVariant for medium gray
                                ),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        // Search Input
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Buscar campo") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(8.dp), // Slightly rounded corners
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary, // Primary color on focus
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) // Gray border
                            )
                        )

                        // Separator before the list of units, if needed. For now, a Spacer.
                        Spacer(modifier = Modifier.height(8.dp))

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    // Filter units based on search query
                    val filteredUnidades = uiState.unidades?.filter {
                        it.nombre?.contains(searchQuery, ignoreCase = true) == true ||
                                it.identificadorLocal?.contains(searchQuery, ignoreCase = true) == true
                    } ?: emptyList()

                    items(filteredUnidades) { unidad ->
                        CampoListItem(
                            unidad = unidad,
                            isEnabled = true, // For now, all units are enabled
                            onUnidadSelected = { selectedUnidad ->
                                navController.navigate(Routes.createMovimientoFormRoute(selectedUnidad.id.toString()))
                            }
                        )
                    }
                }
            }
        }
    }
}