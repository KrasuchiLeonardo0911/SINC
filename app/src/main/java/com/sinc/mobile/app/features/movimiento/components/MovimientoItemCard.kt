package com.sinc.mobile.app.features.movimiento.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinc.mobile.app.features.movimiento.MovimientoAgrupado
import com.sinc.mobile.domain.model.Catalogos
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientoItemCard(
    movimiento: MovimientoAgrupado,
    catalogos: Catalogos?,
    unidades: List<UnidadProductiva>,
    onDelete: (MovimientoAgrupado) -> Unit
) {
    val motivo = catalogos?.motivosMovimiento?.find { it.id == movimiento.motivoMovimientoId }
    val isAlta = motivo?.tipo?.contains("alta", ignoreCase = true) == true
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
                Icon(icon, contentDescription = motivo?.tipo, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val especie = catalogos?.especies?.find { it.id == movimiento.especieId }?.nombre ?: "N/A"
                val categoria = catalogos?.categorias?.find { it.id == movimiento.categoriaId }?.nombre ?: "N/A"

                Text(
                    "$especie - $categoria",
                    fontWeight = FontWeight.SemiBold,
                    color = colorTextoPrincipal
                )
                Text(
                    motivo?.nombre ?: "N/A",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorTextoSecundario
                )
            }
            Text(
                text = (if (isAlta) "+" else "-") + movimiento.cantidadTotal.toString(),
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { onDelete(movimiento) }) {
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
