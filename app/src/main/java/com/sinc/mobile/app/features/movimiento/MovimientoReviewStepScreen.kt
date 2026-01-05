package com.sinc.mobile.app.features.movimiento

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinc.mobile.domain.model.MovimientoPendiente
import com.sinc.mobile.ui.theme.SincMobileTheme
import java.time.LocalDateTime


val MovementGreen = Color(0xFF28A745)
val MovementRed = Color(0xFFDC3545)

@Composable
fun MovimientoReviewStepContent(
    movimientos: List<MovimientoPendiente>,
    onEdit: (Long) -> Unit,
    onDelete: (MovimientoPendiente) -> Unit
) {
    if (movimientos.isEmpty()) {
        EmptyStateReview()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Revisar Movimientos",
                        style = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Revise y confirme los datos.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.DarkGray)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
            items(movimientos, key = { it.id }) { movement ->
                PendingMovementItemRow(
                    movement = movement,
                    onEdit = onEdit,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
fun PendingMovementItemRow(
    movement: MovimientoPendiente,
    onEdit: (Long) -> Unit,
    onDelete: (MovimientoPendiente) -> Unit
) {
    // This logic needs to be based on the real model now.
    // We'll assume a 'type' property or infer it from 'motivo'.
    // For the mockup, we'll make a guess.
    val isAlta = movement.motivoMovimientoId < 10 // Hypothetical IDs for altas vs bajas
    val indicatorColor = if (isAlta) MovementGreen else MovementRed
    val indicatorIcon = if (isAlta) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward
    val indicatorText = if (isAlta) "Alta" else "Baja"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = indicatorIcon,
                    contentDescription = indicatorText,
                    tint = indicatorColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = indicatorText.uppercase(),
                    color = indicatorColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }

            // Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${movement.cantidad} x Especie ID: ${movement.especieId}", // Simplified text
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Motivo ID: ${movement.motivoMovimientoId}", // Simplified text
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Actions
            Row {
                IconButton(onClick = { onEdit(movement.id) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onDelete(movement) }) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateReview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "No hay movimientos",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Text(
                text = "No hay movimientos pendientes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Los movimientos que agregues en el formulario aparecerán aquí para que los revises antes de guardarlos.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Preview(showBackground = true, name = "Review Screen Content")
@Composable
fun MovimientoReviewStepContentPreview() {
    val sampleMovements = listOf(
        MovimientoPendiente(1, 1, 1, 1, 1, 5, 1, null, null, LocalDateTime.now(), false),
        MovimientoPendiente(2, 1, 2, 2, 2, 3, 2, null, null, LocalDateTime.now(), false),
        MovimientoPendiente(3, 1, 1, 3, 3, 2, 11, null, null, LocalDateTime.now(), false), // Baja
    )
    SincMobileTheme {
        MovimientoReviewStepContent(
            movimientos = sampleMovements,
            onEdit = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true, name = "Review Screen Empty")
@Composable
fun MovimientoReviewStepScreenEmptyPreview() {
    SincMobileTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            EmptyStateReview()
        }
    }
}
