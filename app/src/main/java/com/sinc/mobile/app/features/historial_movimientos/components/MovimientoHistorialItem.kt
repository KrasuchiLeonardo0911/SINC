package com.sinc.mobile.app.features.historial_movimientos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinc.mobile.R
import com.sinc.mobile.domain.model.MovimientoHistorial
import java.time.format.DateTimeFormatter

@Composable
fun MovimientoHistorialItem(
    movimiento: MovimientoHistorial,
    modifier: Modifier = Modifier
) {
    val isAlta = movimiento.tipoMovimiento.equals("alta", ignoreCase = true)
    val backgroundColor = if (isAlta) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val iconRes = if (isAlta) R.drawable.ic_arrow_upward else R.drawable.ic_arrow_downward
    val iconColor = if (isAlta) Color(0xFF388E3C) else Color(0xFFD32F2F)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(backgroundColor, shape = MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = movimiento.tipoMovimiento,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = movimiento.motivo,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        fontSize = 17.sp
                    )
                    Text(
                        text = movimiento.fechaRegistro.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "${if (isAlta) "+" else "-"}${movimiento.cantidad}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = iconColor
                    ),
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "Especie:", value = "${movimiento.especie} (${movimiento.raza})")
            InfoRow(label = "Categoría:", value = movimiento.categoria)
            InfoRow(label = "Unidad Prod.:", value = movimiento.unidadProductiva)
            val destino = movimiento.destinoTraslado
            if (destino != null) {
                InfoRow(label = "Destino:", value = destino)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.width(100.dp)
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
