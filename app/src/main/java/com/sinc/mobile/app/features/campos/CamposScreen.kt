package com.sinc.mobile.app.features.campos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sinc.mobile.app.features.movimiento.components.CampoListItem
import com.sinc.mobile.app.ui.components.EmptyState
import com.sinc.mobile.app.ui.components.MinimalHeader
import androidx.compose.ui.text.TextStyle


@Composable
fun CamposScreen(
    viewModel: CamposViewModel = hiltViewModel(),
    mainScaffoldBottomPadding: Dp,
    onNavigateToCreateUnidadProductiva: () -> Unit,
    onBack: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }

    // Listen for the result from CreateUnidadProductivaScreen
    val currentBackStackEntry = navController.currentBackStackEntry
    LaunchedEffect(currentBackStackEntry) {
        val shouldRefresh = currentBackStackEntry?.savedStateHandle?.get<Boolean>("should_refresh_ups")
        if (shouldRefresh == true) {
            viewModel.syncUnidadesProductivas()
            currentBackStackEntry.savedStateHandle.remove<Boolean>("should_refresh_ups")
        }
    }

    Scaffold(
        modifier = modifier.navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MinimalHeader(
                title = "Mis Campos",
                onBackPress = onBack,
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = mainScaffoldBottomPadding),
        ) {
            if (uiState.isLoading && uiState.unidades.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar campo por nombre o RNSPA") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )

                    val filteredUnidades = uiState.unidades.filter {
                        it.nombre?.contains(searchQuery, ignoreCase = true) == true ||
                                it.identificadorLocal?.contains(searchQuery, ignoreCase = true) == true
                    }

                    if (filteredUnidades.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            EmptyState(
                                title = if (searchQuery.isNotBlank()) "No se encontraron campos" else "Aún no tienes campos registrados.",
                                message = if (searchQuery.isNotBlank()) "Intenta con otra búsqueda." else "Registra tu primer campo usando el botón de agregar.",
                                icon = Icons.Outlined.Map,
                                iconSize = 48.dp,
                                titleStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                messageStyle = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 72.dp) // Adjusted padding for FAB
                        ) {
                            items(filteredUnidades, key = { it.id }) { unidad ->
                                CampoListItem(
                                    unidad = unidad,
                                    isEnabled = false, // Not clickable
                                    onUnidadSelected = { /* No action */ }
                                )
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = onNavigateToCreateUnidadProductiva,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(52.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, "Registrar Campo")
            }
        }
    }
}