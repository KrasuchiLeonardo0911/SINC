package com.sinc.mobile.app.features.movimiento.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sinc.mobile.app.features.movimiento.MovimientoAgrupado
import com.sinc.mobile.domain.model.Catalogos
import com.sinc.mobile.domain.model.UnidadProductiva
import androidx.compose.material3.MaterialTheme

@Composable
fun MovimientosPendientesTable(
    movimientos: List<MovimientoAgrupado>,
    catalogos: Catalogos?,
    unidades: List<UnidadProductiva>,
    onDelete: (MovimientoAgrupado) -> Unit
) {
    if (movimientos.isEmpty()) {
        Text(
            "No hay movimientos pendientes.",
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }

    // Usamos un Column en lugar de LazyColumn porque ya estamos dentro de una LazyColumn padre
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        movimientos.forEach { movimiento ->
            MovimientoItemCard(movimiento, catalogos, unidades, onDelete)
        }
    }
}
