package com.sinc.mobile.app.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.sinc.mobile.domain.model.venta.LogisticaStatus

data class StatusUiModel(
    val label: String,
    val containerColor: Color,
    val contentColor: Color,
    val icon: ImageVector? = null,
    val canCancel: Boolean = false,
    val stepIndex: Int = 0 // Para el stepper: 0=Comprometido, 1=Recogido, 2=Faena, 3=Entregado
)

object LogisticaUiMapper {
    fun getStatusUi(statusKey: String): StatusUiModel {
        val status = LogisticaStatus.fromKey(statusKey)
        return when (status) {
            LogisticaStatus.COMPROMETIDO -> StatusUiModel(
                label = "Comprometido",
                containerColor = Color(0xFFE3F2FD), // Azul claro
                contentColor = Color(0xFF1565C0),   // Azul oscuro
                icon = Icons.Default.Schedule,
                canCancel = true,
                stepIndex = 0
            )
            LogisticaStatus.RECOGIDO -> StatusUiModel(
                label = "Recogido",
                containerColor = Color(0xFFE8F5E9), // Verde claro
                contentColor = Color(0xFF2E7D32),   // Verde oscuro
                icon = Icons.Default.LocalShipping,
                canCancel = false,
                stepIndex = 1
            )
            LogisticaStatus.EN_MATADERO -> StatusUiModel(
                label = "En Planta",
                containerColor = Color(0xFFFFF3E0), // Naranja claro
                contentColor = Color(0xFFEF6C00),   // Naranja oscuro
                icon = Icons.Default.Factory,
                canCancel = false,
                stepIndex = 2
            )
            LogisticaStatus.ENTREGADO -> StatusUiModel(
                label = "Finalizado",
                containerColor = Color(0xFFF1F8E9),
                contentColor = Color(0xFF558B2F),
                icon = Icons.Default.CheckCircle,
                canCancel = false,
                stepIndex = 3
            )
            LogisticaStatus.RECHAZADO_CARGA, 
            LogisticaStatus.MATADERO_RECHAZADO, 
            LogisticaStatus.RECHAZADO_FINAL -> StatusUiModel(
                label = "Rechazado",
                containerColor = Color(0xFFFFEBEE), // Rojo claro
                contentColor = Color(0xFFC62828),   // Rojo oscuro
                icon = Icons.Default.Error,
                canCancel = false,
                stepIndex = -1 // Error
            )
            LogisticaStatus.CANCELADO_REGRESANDO -> StatusUiModel(
                label = "Cancelado",
                containerColor = Color(0xFFECEFF1),
                contentColor = Color(0xFF455A64),
                icon = Icons.Default.Cancel,
                canCancel = false,
                stepIndex = -1
            )
            else -> StatusUiModel(
                label = "Pendiente",
                containerColor = Color(0xFFF5F5F5),
                contentColor = Color.Gray,
                icon = Icons.Default.HelpOutline,
                canCancel = true // Asumimos retrocompatibilidad
            )
        }
    }
}
