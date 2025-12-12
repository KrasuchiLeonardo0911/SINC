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
import com.sinc.mobile.app.ui.components.FormDropdown
import com.sinc.mobile.app.ui.components.FormFieldWrapper
import com.sinc.mobile.ui.theme.AccentYellow
import com.sinc.mobile.ui.theme.SincMobileTheme

@Composable
fun MovimientoForm(
    modifier: Modifier = Modifier
    // TODO: Add back state and event handlers
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp) // Original spacing
    ) {
        // --- Especie ---
        FormFieldWrapper(label = "Especie") { modifier ->
            FormDropdown<String>(
                items = listOf("Ovino", "Caprino", "Bovino"),
                selectedItem = "Ovino",
                onItemSelected = { /*TODO*/ },
                itemToString = { it },
                modifier = modifier
            )
        }

        // --- Categoria ---
        FormFieldWrapper(label = "Categoría") { modifier ->
            FormDropdown<String>(
                items = listOf("Cordero", "Oveja", "Carnero"),
                selectedItem = null,
                onItemSelected = { /*TODO*/ },
                itemToString = { it },
                placeholder = "Seleccionar categoría",
                modifier = modifier
            )
        }
        
        // --- Raza ---
        FormFieldWrapper(label = "Raza") { modifier ->
            FormDropdown<String>(
                items = listOf("Merino", "Corriedale", "Pampinta"),
                selectedItem = null,
                onItemSelected = { /*TODO*/ },
                itemToString = { it },
                placeholder = "Seleccionar raza",
                modifier = modifier
            )
        }

        // --- Motivo ---
        FormFieldWrapper(label = "Motivo") { modifier ->
            FormDropdown<String>(
                items = listOf("Nacimiento", "Compra", "Traslado"),
                selectedItem = "Nacimiento",
                onItemSelected = { /*TODO*/ },
                itemToString = { it },
                modifier = modifier
            )
        }
        
        // --- Cantidad ---
        FormFieldWrapper(label = "Cantidad") { modifier ->
            var cantidad by remember { mutableStateOf("1") }
            OutlinedTextField(
                value = cantidad,
                onValueChange = { cantidad = it },
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
                    fontSize = 16.sp, // Original font size
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(Modifier.height(8.dp)) // Original spacing

        // --- Botón de Guardar ---
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp), // Original height
            shape = ButtonDefaults.shape, // Original shape
            colors = ButtonDefaults.buttonColors(containerColor = AccentYellow)
        ) {
            Text(
                text = "Guardar",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp, // Original font size
                color = Color.Black // Texto negro
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun MovimientoFormPreview() {
    SincMobileTheme {
        Box(Modifier.padding(16.dp)) {
            MovimientoForm()
        }
    }
}
