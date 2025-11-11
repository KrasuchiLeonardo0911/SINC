package com.sinc.mobile.app.features.movimiento.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sinc.mobile.app.features.movimiento.MovimientoSyncState

@Composable
fun SyncSection(state: MovimientoSyncState, onSync: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onSync,
            enabled = state.movimientosAgrupados.isNotEmpty() && !state.isSyncing
        ) {
            if (state.isSyncing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sincronizando...")
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            } else {
                Text("Sincronizar Cambios")
            }
        }

        if (state.syncSuccess) {
            Text(
                "Sincronización completada con éxito.",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        state.syncError?.let {
            Text(
                text = "Error: $it",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
