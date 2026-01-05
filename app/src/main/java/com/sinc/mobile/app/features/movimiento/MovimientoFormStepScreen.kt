package com.sinc.mobile.app.features.movimiento

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinc.mobile.domain.model.Categoria
import com.sinc.mobile.domain.model.Especie
import com.sinc.mobile.domain.model.MotivoMovimiento
import com.sinc.mobile.domain.model.Raza
import com.sinc.mobile.ui.theme.SincMobileTheme


// --- Bottom Sheet Specific Data Class ---
sealed class SheetContent {
    data class CategoriaSheet(val items: List<Categoria>) : SheetContent()
    data class RazaSheet(val items: List<Raza>) : SheetContent()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientoFormStepContent(
    formState: MovimientoFormState,
    onEspecieSelected: (Especie) -> Unit,
    onCategoriaSelected: (Categoria) -> Unit,
    onRazaSelected: (Raza) -> Unit,
    onMotivoSelected: (MotivoMovimiento) -> Unit,
    onCantidadChanged: (String) -> Unit,
    onDestinoChanged: (String) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    var sheetContent by remember { mutableStateOf<SheetContent?>(null) }
    var showDestinoSheet by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // --- Bottom sheet for Categoría and Raza ---
    if (showSheet && sheetContent != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            when (val content = sheetContent) {
                is SheetContent.CategoriaSheet -> {
                    SheetContentLayout(
                        title = "Seleccionar Categoría",
                        items = content.items,
                        getItemName = { it.nombre },
                        onItemSelected = {
                            onCategoriaSelected(it)
                            showSheet = false
                        }
                    )
                }
                is SheetContent.RazaSheet -> {
                    SheetContentLayout(
                        title = "Seleccionar Raza",
                        items = content.items,
                        getItemName = { it.nombre },
                        onItemSelected = {
                            onRazaSelected(it)
                            showSheet = false
                        }
                    )
                }
                else -> {}
            }
        }
    }

    // --- Bottom sheet for Destino ---
    if (showDestinoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDestinoSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "Información de Destino", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = formState.destino,
                    onValueChange = onDestinoChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = { Text("Ej: Establecimiento vecino") },
                    shape = RoundedCornerShape(16.dp),
                )
                Button(onClick = { showDestinoSheet = false }) {
                    Text("Guardar Destino")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
            // Request focus when the sheet is shown
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
    }

    // --- Trigger for Destino Sheet ---
    LaunchedEffect(formState.selectedMotivo) {
        if (formState.selectedMotivo?.nombre == "Traslado (Salida)") {
            showDestinoSheet = true
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Cargar Movimiento",
                    style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Complete los datos del lote.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.DarkGray)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // 3. Sección "Especie"
        item {
            EspecieSelector(
                selectedEspecie = formState.selectedEspecie,
                especies = formState.filteredEspecies,
                onSelected = onEspecieSelected
            )
        }

        // 4. Campos de Selección
        item {
            DropdownField(
                label = "Categoría",
                selectedValue = formState.selectedCategoria?.nombre,
                placeholder = "Seleccionar categoría",
                enabled = formState.selectedEspecie != null,
                onClick = {
                    sheetContent = SheetContent.CategoriaSheet(formState.filteredCategorias)
                    showSheet = true
                }
            )
        }
        item {
            DropdownField(
                label = "Raza",
                selectedValue = formState.selectedRaza?.nombre,
                placeholder = "Seleccionar raza",
                enabled = formState.selectedEspecie != null,
                onClick = {
                    sheetContent = SheetContent.RazaSheet(formState.filteredRazas)
                    showSheet = true
                }
            )
        }

        // 5. Sección "Motivo"
        item {
            MotivoSelector(
                selectedMotivo = formState.selectedMotivo,
                motivos = formState.filteredMotivos,
                onSelected = onMotivoSelected
            )
        }

        // Additional spacing between Motivo and Cantidad
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // 6. Sección "Cantidad"
        item {
            QuantityStepper(
                cantidad = formState.cantidad,
                onCantidadChanged = onCantidadChanged
            )
        }

        // The old destino field is now removed from here.
    }
}

// --- Bottom Sheet Specific Composables ---

@Composable
fun <T> SheetContentLayout(
    title: String,
    items: List<T>,
    getItemName: (T) -> String,
    onItemSelected: (T) -> Unit
) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(items) { item ->
            Text(
                text = getItemName(item),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemSelected(item) }
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
        }
        item {
            Spacer(modifier = Modifier.height(32.dp)) // Extra space at the bottom
        }
    }
}


// --- Existing Composables (with modifications) ---

@Composable
fun SectionHeader(title: String, action: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        action()
    }
}

@Composable
fun EspecieSelector(
    selectedEspecie: Especie?,
    especies: List<Especie>,
    onSelected: (Especie) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Especie", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            especies.forEach { especie ->
                val isSelected = selectedEspecie?.id == especie.id
                Chip(
                    label = especie.nombre,
                    isSelected = isSelected,
                    onSelected = { onSelected(especie) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    selectedValue: String?,
    placeholder: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        Box {
            OutlinedTextField(
                value = selectedValue ?: "",
                onValueChange = {},
                placeholder = { Text(placeholder) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Desplegable"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                readOnly = true,
                enabled = false, // Always disabled to be purely visual
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.4f else 0.2f),
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            )
            // Transparent overlay to capture clicks
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClick = onClick, enabled = enabled)
            )
        }
    }
}

@Composable
fun MotivoSelector(
    selectedMotivo: MotivoMovimiento?,
    motivos: List<MotivoMovimiento>,
    onSelected: (MotivoMovimiento) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Motivo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "(Deslice)",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(motivos) { motivo ->
                val isSelected = selectedMotivo?.id == motivo.id
                Chip(
                    label = motivo.nombre,
                    isSelected = isSelected,
                    onSelected = { onSelected(motivo) }
                )
            }
        }
    }
}

@Composable
fun Chip(label: String, isSelected: Boolean, onSelected: () -> Unit) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primary // Bordó
    } else {
        Color.Transparent // Blanco
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary // Texto blanco
    } else {
        MaterialTheme.colorScheme.onSurface // Texto oscuro estándar
    }
    val border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.primary) // Borde bordó

    Card(
        modifier = Modifier.clickable { onSelected() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = border
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), // Reduced padding for smaller size
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            fontSize = 14.sp // Slightly smaller font size
        )
    }
}

@Composable
fun QuantityStepper(
    cantidad: String,
    onCantidadChanged: (String) -> Unit
) {
    val count = cantidad.toIntOrNull() ?: 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Cantidad", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StepperButton(icon = { Icon(Icons.Default.Remove, contentDescription = "Quitar") }, enabled = count > 0) { onCantidadChanged((count - 1).toString()) }
            Text(text = count.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            StepperButton(icon = { Icon(Icons.Default.Add, contentDescription = "Agregar") }, isPrimary = true) { onCantidadChanged((count + 1).toString()) }
        }
    }
}

@Composable
fun StepperButton(icon: @Composable () -> Unit, isPrimary: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    val containerColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        isPrimary -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        isPrimary -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .background(color = containerColor, shape = RoundedCornerShape(12.dp)),
        enabled = enabled
    ) {
        icon()
    }
}

@Preview(showBackground = true)

@Composable

fun MovimientoFormStepContentPreview() {

    SincMobileTheme {

        MovimientoFormStepContent(

            formState = MovimientoFormState(),

            onEspecieSelected = {},

            onCategoriaSelected = {},

            onRazaSelected = {},

            onMotivoSelected = {},

            onCantidadChanged = {},

            onDestinoChanged = {}

        )

    }

}