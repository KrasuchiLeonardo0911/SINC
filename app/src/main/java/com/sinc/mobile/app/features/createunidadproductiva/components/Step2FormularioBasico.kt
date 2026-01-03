package com.sinc.mobile.app.features.createunidadproductiva.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.sinc.mobile.app.features.createunidadproductiva.IdentifierFormatInfo
import com.sinc.mobile.app.ui.components.visual_transformation.PatternVisualTransformation
import com.sinc.mobile.domain.model.IdentifierConfig

@Composable
fun Step2FormularioBasico(
    nombre: String,
    onNombreChange: (String) -> Unit,
    nombreError: String?,
    identifierValue: String,
    onIdentifierValueChange: (String) -> Unit,
    identifierError: String?,
    selectedIdentifierConfig: IdentifierConfig?,
    identifierFormatInfo: IdentifierFormatInfo?,
    onIdentifierHelpClick: () -> Unit,
    superficie: String,
    onSuperficieChange: (String) -> Unit,
    superficieError: String?,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp) // Reduced space
            ) {
                // Nombre del campo
                OutlinedTextField(
                    value = nombre,
                    onValueChange = onNombreChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre del campo") },
                    placeholder = { Text("Ej: Campo Las Marías") },
                    singleLine = true,
                    isError = nombreError != null
                )
                if (nombreError != null) {
                    Text(
                        text = nombreError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))


                // Dynamic Identifier (RNSPA, etc.)
                val identifierLabel = selectedIdentifierConfig?.type?.uppercase() ?: "Identificador"
                OutlinedTextField(
                    value = identifierValue,
                    onValueChange = onIdentifierValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(identifierLabel) },
                    placeholder = { Text(selectedIdentifierConfig?.hint ?: "") },
                    singleLine = true,
                    isError = identifierError != null,
                    visualTransformation = identifierFormatInfo?.pattern?.let { PatternVisualTransformation(it) } ?: VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (identifierError != null) {
                    Text(
                        text = identifierError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                TextButton(
                    onClick = onIdentifierHelpClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "No conozco mi $identifierLabel",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Superficie
                OutlinedTextField(
                    value = superficie,
                    onValueChange = onSuperficieChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Superficie (hectáreas)") },
                    placeholder = { Text("Ej: 150.5") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = superficieError != null
                )
                if (superficieError != null) {
                    Text(
                        text = superficieError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}