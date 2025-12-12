package com.sinc.mobile.app.features.movimiento.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinc.mobile.app.features.movimiento.MovimientoFormState
import com.sinc.mobile.app.ui.components.FormDropdown
import com.sinc.mobile.app.ui.components.FormFieldWrapper
import com.sinc.mobile.domain.model.Categoria
import com.sinc.mobile.domain.model.Especie
import com.sinc.mobile.domain.model.MotivoMovimiento
import com.sinc.mobile.domain.model.Raza
import com.sinc.mobile.ui.theme.AccentYellow
import com.sinc.mobile.ui.theme.SincMobileTheme

@Composable
fun MovimientoForm(
    modifier: Modifier = Modifier,
    formState: MovimientoFormState,
    onEspecieSelected: (Especie) -> Unit,
    onCategoriaSelected: (Categoria) -> Unit,
    onRazaSelected: (Raza) -> Unit,
    onMotivoSelected: (MotivoMovimiento) -> Unit,
    onCantidadChanged: (String) -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Especie ---
        FormFieldWrapper(label = "Especie") { modifier ->
            FormDropdown(
                items = formState.filteredEspecies,
                selectedItem = formState.selectedEspecie,
                onItemSelected = onEspecieSelected,
                itemToString = { it.nombre },
                modifier = modifier
            )
        }

        // --- Categoria ---
        FormFieldWrapper(label = "Categoría") { modifier ->
            FormDropdown(
                items = formState.filteredCategorias,
                selectedItem = formState.selectedCategoria,
                onItemSelected = onCategoriaSelected,
                itemToString = { it.nombre },
                placeholder = "Seleccionar categoría",
                modifier = modifier,
                enabled = formState.selectedEspecie != null
            )
        }

        // --- Raza ---
        FormFieldWrapper(label = "Raza") { modifier ->
            FormDropdown(
                items = formState.filteredRazas,
                selectedItem = formState.selectedRaza,
                onItemSelected = onRazaSelected,
                itemToString = { it.nombre },
                placeholder = "Seleccionar raza",
                modifier = modifier,
                enabled = formState.selectedEspecie != null
            )
        }

        // --- Motivo ---
        FormFieldWrapper(label = "Motivo") { modifier ->
            FormDropdown(
                items = formState.filteredMotivos,
                selectedItem = formState.selectedMotivo,
                onItemSelected = onMotivoSelected,
                itemToString = { it.nombre },
                modifier = modifier
            )
        }

        // --- Cantidad ---
        FormFieldWrapper(label = "Cantidad") { modifier ->
            OutlinedTextField(
                value = formState.cantidad,
                onValueChange = onCantidadChanged,
                modifier = modifier,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                placeholder = { Text("0") },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(Modifier.height(8.dp))

        // --- Botón de Guardar ---
        Button(
            onClick = onSave,
            enabled = formState.isFormValid && !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = ButtonDefaults.shape,
            colors = ButtonDefaults.buttonColors(containerColor = AccentYellow)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.Black
                )
            } else {
                Text(
                    text = "Guardar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
        }
    }
}