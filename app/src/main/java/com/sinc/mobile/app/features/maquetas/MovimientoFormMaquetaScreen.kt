package com.sinc.mobile.app.features.maquetas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.ui.theme.SincMobileTheme
import kotlinx.coroutines.launch

// --- Bottom Sheet Specific Data Class ---
sealed class SheetContent(val title: String, val items: List<String>) {
    object Categoria : SheetContent("Seleccionar Categoría", listOf("Cordero/a", "Borrego/a", "Oveja", "Capón", "Carnero"))
    object Raza : SheetContent("Seleccionar Raza", listOf("Criolla", "Corriedale", "Merino", "Texel", "Ideal"))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientoFormMaquetaContent() {
    var showSheet by remember { mutableStateOf(false) }
    var sheetContent by remember { mutableStateOf<SheetContent?>(null) }

    if (showSheet && sheetContent != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            SheetContentLayout(
                content = sheetContent!!,
                onItemSelected = {
                    showSheet = false
                    // Here you would update the actual form state
                }
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = com.sinc.mobile.R.drawable.ilustracion_ovinos_maqueta),
                    contentDescription = "Ilustración de Animales",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // 3. Sección "Especie"
        item {
            EspecieSelector()
        }

        // 4. Campos de Selección
        item {
            DropdownField(
                label = "Categoría",
                placeholder = "Seleccionar categoría",
                onClick = {
                    sheetContent = SheetContent.Categoria
                    showSheet = true
                }
            )
        }
        item {
            DropdownField(
                label = "Raza",
                placeholder = "Seleccionar raza",
                onClick = {
                    sheetContent = SheetContent.Raza
                    showSheet = true
                }
            )
        }

        // 5. Sección "Motivo"
        item {
            MotivoSelector()
        }

        // 6. Sección "Cantidad"
        item {
            QuantityStepper()
        }

        // Add extra space at the bottom to ensure scrolling is possible
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// --- Bottom Sheet Specific Composables ---

@Composable
fun SheetContentLayout(content: SheetContent, onItemSelected: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(text = content.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(content.items) { item ->
            Text(
                text = item,
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
fun EspecieSelector() {
    var selectedEspecie by remember { mutableStateOf<String?>("Ovinos") } // Default to Ovinos
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Especie", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val especies = listOf("Ovinos", "Caprinos")
            especies.forEach { especie ->
                val isSelected = selectedEspecie == especie
                Chip(
                    label = especie,
                    isSelected = isSelected,
                    onSelected = { selectedEspecie = it }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(label: String, placeholder: String, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        Box(
            modifier = Modifier.clickable(onClick = onClick)
        ) {
            OutlinedTextField(
                value = "",
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
                enabled = false, // To make it non-interactive but clickable
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            )
        }
    }
}

@Composable
fun MotivoSelector() {
    var selectedMotivo by remember { mutableStateOf<String?>(null) }
    val motivos = listOf("Nacimiento", "Compra", "Muerte", "Venta", "Traslado")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "Motivo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(motivos) { motivo ->
                val isSelected = selectedMotivo == motivo
                Chip(
                    label = motivo,
                    isSelected = isSelected,
                    onSelected = { selectedMotivo = it }
                )
            }
        }
    }
}

@Composable
fun Chip(label: String, isSelected: Boolean, onSelected: (String) -> Unit) {
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
        modifier = Modifier.clickable { onSelected(label) },
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
fun QuantityStepper() {
    var count by remember { mutableStateOf(0) }
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
            StepperButton(icon = { Icon(Icons.Default.Remove, contentDescription = "Quitar") }, enabled = count > 0) { count-- }
            Text(text = count.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            StepperButton(icon = { Icon(Icons.Default.Add, contentDescription = "Agregar") }, isPrimary = true) { count++ }
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
fun MovimientoFormMaquetaContentPreview() {
    SincMobileTheme {
        MovimientoFormMaquetaContent()
    }
}
