package com.sinc.mobile.app.features.agenda.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinc.mobile.domain.model.agenda.AgendaItem
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaDetailSheet(
    item: AgendaItem,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM", Locale("es", "ES"))
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = item.titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.fechaProgramada.format(dateFormatter).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = " • ${item.fechaProgramada.format(timeFormatter)} hs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            val description = item.descripcion
            if (!description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.LightGray.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = description,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón Marcar como Hecha (Solo si no está completada)
                if (!item.isCompleted) {
                    OutlinedButton(
                        onClick = {
                            onToggleStatus()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp), // Reducimos padding interno
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Hecha",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                }

                // Botón Eliminar
                Button(
                    onClick = {
                        onDelete()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red.copy(alpha = 0.1f),
                        contentColor = Color.Red
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Eliminar", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
