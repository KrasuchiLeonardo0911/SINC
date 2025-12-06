package com.sinc.mobile.app.features.createunidadproductiva.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sinc.mobile.domain.model.GenericError
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error

@Composable
fun RnspaRequestModal(
    show: Boolean,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    municipio: String,
    onMunicipioChange: (String) -> Unit,
    paraje: String,
    onParajeChange: (String) -> Unit,
    direccion: String,
    onDireccionChange: (String) -> Unit,
    infoAdicional: String,
    onInfoAdicionalChange: (String) -> Unit,
    isLoading: Boolean,
    result: Result<Unit, Error>?,
    identifierLabel: String
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface, // Set background to white
            title = { Text("Solicitar $identifierLabel") },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        if (isLoading) {
                            CircularProgressIndicator()
                        } else if (result != null) {
                            when (result) {
                                is Result.Success -> {
                                    Text("¡Solicitud enviada con éxito! Un administrador se pondrá en contacto contigo a la brevedad.")
                                    LaunchedEffect(Unit) {
                                        kotlinx.coroutines.delay(3000)
                                        onDismiss()
                                    }
                                }
                                is Result.Failure -> {
                                    val error = result.error as? GenericError
                                    Text("Error: ${error?.message ?: "Ocurrió un error desconocido."}")
                                }
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Completa los siguientes datos para que un administrador pueda ayudarte a encontrar tu número.")
                                OutlinedTextField(
                                    value = municipio,
                                    onValueChange = onMunicipioChange,
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Municipio") },
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = paraje,
                                    onValueChange = onParajeChange,
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Paraje (Opcional)") },
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = direccion,
                                    onValueChange = onDireccionChange,
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Dirección (Calle y número)") },
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = infoAdicional,
                                    onValueChange = onInfoAdicionalChange,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    label = { Text("Información adicional (Recomendado)") }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (result == null && !isLoading) {
                    Button(onClick = onSubmit) {
                        Text("Enviar")
                    }
                }
            },
            dismissButton = {
                if (result == null && !isLoading) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                } else if (result is Result.Failure) {
                    TextButton(onClick = onDismiss) {
                        Text("Cerrar")
                    }
                }
            }
        )
    }
}
