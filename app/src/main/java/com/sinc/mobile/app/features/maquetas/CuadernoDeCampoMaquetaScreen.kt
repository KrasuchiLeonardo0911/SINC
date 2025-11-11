package com.sinc.mobile.app.features.maquetas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinc.mobile.ui.theme.SincMobileTheme

// --- Paleta de Colores del Ejemplo ---
val colorAlta = Color(0xFF10b981) // emerald-500
val colorBaja = Color(0xFFf43f5e) // rose-500
val colorSuperficie = Color.White
val colorBorde = Color(0xFFe2e8f0) // slate-200
val colorFondo = Color(0xFFf8fafc) // slate-50
val colorTextoPrincipal = Color(0xFF0f172a) // slate-900
val colorTextoSecundario = Color(0xFF64748b) // slate-500

// --- Datos de Maqueta ---
data class MaquetaUnidadProductiva(val id: Int, val nombre: String, val localidad: String)
data class MaquetaMovimiento(val id: Int, val tipo: String, val cantidad: Int, val especie: String, val categoria: String, val motivo: String)

val maquetaUnidades = listOf(
    MaquetaUnidadProductiva(1, "El Destino", "Tandil"),
    MaquetaUnidadProductiva(2, "La Esperanza", "Azul"),
    MaquetaUnidadProductiva(3, "Los Pinos", "Rauch")
)
val maquetaMovimientosPendientes = listOf(
    MaquetaMovimiento(1, "alta", 12, "Ovino", "Cordero/a", "Nacimiento"),
    MaquetaMovimiento(2, "baja", 5, "Ovino", "Oveja Adulta", "Venta"),
    MaquetaMovimiento(3, "alta", 8, "Caprino", "Cabrito/a", "Compra")
)

// --- Componentes de la Maqueta ---

@Composable
fun CuadernoDeCampoMaquetaScreen() {
    var selectedUnidad by remember { mutableStateOf<MaquetaUnidadProductiva?>(maquetaUnidades.first()) }
    var selectedAction by remember { mutableStateOf<String?>(null) } // "alta" o "baja"

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .background(colorSuperficie.copy(alpha = 0.8f)) // Simula backdrop-blur
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    "Cuaderno de Campo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorTextoPrincipal
                )
            }
        },
        containerColor = colorFondo
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. Selección de Campo ---
            item {
                Text(
                    "Seleccione campo",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colorTextoPrincipal.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )
                UnidadProductivaSelector(
                    unidades = maquetaUnidades,
                    selectedUnidad = selectedUnidad,
                    onUnidadSelected = { selectedUnidad = it }
                )
            }

            // --- 2. Panel de Acciones ---
            item {
                ActionPanel(
                    onActionSelected = { selectedAction = if (selectedAction == it) null else it },
                    selectedAction = selectedAction
                )
            }

            // --- 3. Formulario de Registro (Aparece al seleccionar una acción) ---
            item {
                val currentAction = selectedAction
                AnimatedVisibility(
                    visible = currentAction != null,
                    enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { -40 }),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    if (currentAction != null) {
                        RegistroMovimientoForm(
                            action = currentAction,
                            onDismiss = { selectedAction = null }
                        )
                    }
                }
            }

            // --- 4. Lista de Movimientos Pendientes ---
            if (maquetaMovimientosPendientes.isNotEmpty()) {
                item {
                    Text(
                        "Pendientes de Sincronizar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colorTextoPrincipal.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
                    )
                }
                items(maquetaMovimientosPendientes.size) { index ->
                    MovimientoPendienteItem(movimiento = maquetaMovimientosPendientes[index])
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { /* Lógica de sincronización */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorSuperficie,
                            contentColor = colorTextoPrincipal
                        ),
                        border = BorderStroke(1.dp, colorBorde),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sincronizar")
                        Spacer(Modifier.width(8.dp))
                        Text("Sincronizar Todo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnidadProductivaSelector(
    unidades: List<MaquetaUnidadProductiva>,
    selectedUnidad: MaquetaUnidadProductiva?,
    onUnidadSelected: (MaquetaUnidadProductiva) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorSuperficie),
        border = BorderStroke(1.dp, colorBorde)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colorAlta.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = "Campo",
                        tint = colorAlta,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        selectedUnidad?.nombre ?: "Seleccionar campo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colorTextoPrincipal
                    )
                    Text(
                        selectedUnidad?.localidad ?: "Haga clic para elegir",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorTextoSecundario
                    )
                }
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Desplegar",
                    tint = colorTextoSecundario
                )
            }
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(colorSuperficie)
            ) {
                unidades.forEach { unidad ->
                    DropdownMenuItem(
                        text = { Text(unidad.nombre) },
                        onClick = {
                            onUnidadSelected(unidad)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ActionPanel(onActionSelected: (String) -> Unit, selectedAction: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionButton(
            text = "Registrar Alta",
            icon = Icons.Default.Add,
            isSelected = selectedAction == "alta",
            color = colorAlta,
            onClick = { onActionSelected("alta") },
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            text = "Registrar Baja",
            icon = Icons.Default.Remove,
            isSelected = selectedAction == "baja",
            color = colorBaja,
            onClick = { onActionSelected("baja") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) color else colorSuperficie
    val contentColor = if (isSelected) Color.White else colorTextoPrincipal
    val border = if (isSelected) null else BorderStroke(1.dp, colorBorde)

    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 0.dp),
        border = border
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White.copy(alpha = 0.15f) else colorFondo),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = text, tint = contentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(text, fontWeight = FontWeight.SemiBold, color = contentColor, fontSize = 14.sp)
        }
    }
}

@Composable
fun RegistroMovimientoForm(action: String, onDismiss: () -> Unit) {
    val headerColor = if (action == "alta") colorAlta else colorBaja
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorSuperficie),
        elevation = CardDefaults.cardElevation(4.dp),
        border = BorderStroke(1.dp, colorBorde)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Nueva ${action.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar formulario", tint = Color.White)
                }
            }
            // Form Body
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = "", onValueChange = {}, label = { Text("Especie") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = "", onValueChange = {}, label = { Text("Categoría") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = "", onValueChange = {}, label = { Text("Cantidad") }, modifier = Modifier.fillMaxWidth())
                Button(
                    onClick = { /* Lógica de guardado */ },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = headerColor)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Guardar")
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar Movimiento", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MovimientoPendienteItem(movimiento: MaquetaMovimiento) {
    val isAlta = movimiento.tipo == "alta"
    val color = if (isAlta) colorAlta else colorBaja
    val icon = if (isAlta) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorSuperficie),
        border = BorderStroke(1.dp, colorBorde)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = movimiento.tipo, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${movimiento.especie} - ${movimiento.categoria}",
                    fontWeight = FontWeight.SemiBold,
                    color = colorTextoPrincipal
                )
                Text(
                    movimiento.motivo,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorTextoSecundario
                )
            }
            Text(
                text = (if (isAlta) "+" else "-") + movimiento.cantidad.toString(),
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { /* delete */ }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = colorTextoSecundario.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


// --- Preview ---

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
fun CuadernoDeCampoMaquetaPreview() {
    SincMobileTheme {
        CuadernoDeCampoMaquetaScreen()
    }
}

