package com.sinc.mobile.app.features.createunidadproductiva

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUnidadProductivaScreen(
    viewModel: CreateUnidadProductivaViewModel = hiltViewModel(),
    onUnidadProductivaCreated: (UnidadProductiva) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var identificadorLocal by remember { mutableStateOf("") }
    var superficie by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf("") }
    var longitud by remember { mutableStateOf("") }
    var selectedMunicipio by remember { mutableStateOf<Municipio?>(null) }
    var selectedCondicion by remember { mutableStateOf<CondicionTenencia?>(null) }
    var selectedFuenteAgua by remember { mutableStateOf<FuenteAgua?>(null) }
    var selectedTipoSuelo by remember { mutableStateOf<TipoSuelo?>(null) }
    var selectedTipoPasto by remember { mutableStateOf<TipoPasto?>(null) }

    LaunchedEffect(uiState.unidadProductivaCreated) {
        uiState.unidadProductivaCreated?.let {
            onUnidadProductivaCreated(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Unidad Productiva") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Información Básica", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = identificadorLocal,
                            onValueChange = { identificadorLocal = it },
                            label = { Text("Identificador Local (RNSPA)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = superficie,
                            onValueChange = { superficie = it },
                            label = { Text("Superficie (hectáreas)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Dropdown(
                            label = "Municipio",
                            items = uiState.catalogos?.municipios ?: emptyList(),
                            selectedItem = selectedMunicipio,
                            onItemSelected = { selectedMunicipio = it },
                            itemToString = { it.nombre }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Dropdown(
                            label = "Condición de Tenencia",
                            items = uiState.catalogos?.condicionesTenencia ?: emptyList(),
                            selectedItem = selectedCondicion,
                            onItemSelected = { selectedCondicion = it },
                            itemToString = { it.nombre }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Dropdown(
                            label = "Fuente de Agua",
                            items = uiState.catalogos?.fuentesAgua ?: emptyList(),
                            selectedItem = selectedFuenteAgua,
                            onItemSelected = { selectedFuenteAgua = it },
                            itemToString = { it.nombre }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Dropdown(
                            label = "Tipo de Suelo",
                            items = uiState.catalogos?.tiposSuelo ?: emptyList(),
                            selectedItem = selectedTipoSuelo,
                            onItemSelected = { selectedTipoSuelo = it },
                            itemToString = { it.nombre }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Dropdown(
                            label = "Tipo de Pasto",
                            items = uiState.catalogos?.tiposPasto ?: emptyList(),
                            selectedItem = selectedTipoPasto,
                            onItemSelected = { selectedTipoPasto = it },
                            itemToString = { it.nombre }
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Ubicación", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = latitud,
                                onValueChange = { latitud = it },
                                label = { Text("Latitud") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = longitud,
                                onValueChange = { longitud = it },
                                label = { Text("Longitud") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { /* TODO: Implementar obtener ubicación */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Obtener Ubicación Actual")
                        }
                    }
                }
            }

            item {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            val data = CreateUnidadProductivaData(
                                nombre = nombre,
                                identificadorLocal = identificadorLocal,
                                superficie = superficie.toFloatOrNull() ?: 0f,
                                latitud = latitud.toFloatOrNull() ?: 0f,
                                longitud = longitud.toFloatOrNull() ?: 0f,
                                municipioId = selectedMunicipio?.id ?: 0,
                                condicionTenenciaId = selectedCondicion?.id,
                                fuenteAguaId = selectedFuenteAgua?.id,
                                tipoSueloId = selectedTipoSuelo?.id,
                                tipoPastoId = selectedTipoPasto?.id
                            )
                            viewModel.createUnidadProductiva(data)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Crear Unidad Productiva")
                    }
                }
            }

            item {
                uiState.error?.let { error ->
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> Dropdown(
    label: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    itemToString: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedItem?.let(itemToString) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemToString(item)) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}