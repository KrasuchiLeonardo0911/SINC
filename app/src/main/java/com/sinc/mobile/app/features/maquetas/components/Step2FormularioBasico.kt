package com.sinc.mobile.app.features.maquetas.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sinc.mobile.ui.theme.md_theme_light_primary

@Composable
fun Step2FormularioBasico(
    nombre: String,
    onNombreChange: (String) -> Unit,
    rnspa: String,
    onRnspaChange: (String) -> Unit,
    superficie: String,
    onSuperficieChange: (String) -> Unit,
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nombre del campo
                OutlinedTextField(
                    value = nombre,
                    onValueChange = onNombreChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre del campo") },
                    placeholder = { Text("Ej: Campo Las Marías") },
                    singleLine = true
                )

                // RNSPA
                OutlinedTextField(
                    value = rnspa,
                    onValueChange = onRnspaChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("RNSPA") },
                    placeholder = { Text("Ej: 01.234.5.67890") },
                    singleLine = true
                )
                TextButton(
                    onClick = { /* TODO: Handle RNSPA help */ },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "No conozco mi RNSPA",
                        color = md_theme_light_primary
                    )
                }

                // Superficie
                OutlinedTextField(
                    value = superficie,
                    onValueChange = onSuperficieChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Superficie (hectáreas)") },
                    placeholder = { Text("Ej: 150.5") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
