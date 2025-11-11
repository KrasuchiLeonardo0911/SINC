package com.sinc.mobile.app.features.movimiento.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sinc.mobile.app.features.movimiento.MovimientoSyncManager
import com.sinc.mobile.domain.model.Catalogos
import com.sinc.mobile.domain.model.UnidadProductiva

@Composable
fun MovimientosPendientesSheetContent(
    syncManager: MovimientoSyncManager,
    catalogos: Catalogos?,
    unidades: List<UnidadProductiva>,
    onHeaderClick: () -> Unit
) {
    val syncState = syncManager.syncState.value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pestaña para expandir
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(onClick = onHeaderClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Movimientos Pendientes (${syncState.movimientosAgrupados.size})")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Expandir")
        }

        // Contenido de la tabla y sincronización
        MovimientosPendientesTable(
            movimientos = syncState.movimientosAgrupados,
            catalogos = catalogos,
            unidades = unidades,
            onDelete = syncManager::deleteMovimientoGroup
        )
        Spacer(modifier = Modifier.height(24.dp))
        SyncSection(state = syncState, onSync = syncManager::syncMovements)
        Spacer(modifier = Modifier.height(16.dp))
    }
}
