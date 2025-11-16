package com.sinc.mobile.app.features.createunidadproductiva

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.domain.model.CreateUnidadProductivaData
import com.sinc.mobile.domain.model.UnidadProductiva

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
    var municipioId by remember { mutableStateOf("") } // Temporal, luego será un selector

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
                        // Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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
            OutlinedTextField(
                value = latitud,
                onValueChange = { latitud = it },
                label = { Text("Latitud") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = longitud,
                onValueChange = { longitud = it },
                label = { Text("Longitud") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = municipioId,
                onValueChange = { municipioId = it },
                label = { Text("ID Municipio (Temporal)") }, // Temporal
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

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
                            municipioId = municipioId.toIntOrNull() ?: 0,
                            condicionTenenciaId = null, // Opcional
                            fuenteAguaId = null, // Opcional
                            tipoSueloId = null, // Opcional
                            tipoPastoId = null // Opcional
                        )
                        viewModel.createUnidadProductiva(data)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear Unidad Productiva")
                }
            }

            uiState.error?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
