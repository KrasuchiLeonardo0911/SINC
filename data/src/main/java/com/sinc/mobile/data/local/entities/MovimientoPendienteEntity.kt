package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "movimientos_pendientes")
data class MovimientoPendienteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unidad_productiva_id: Int,
    val especie_id: Int,
    val categoria_id: Int,
    val raza_id: Int,
    val cantidad: Int,
    val motivo_movimiento_id: Int,
    val destino_traslado: String?,
    val observaciones: String?,
    val fecha_registro: LocalDateTime, // Corresponds to backend's fecha_registro
    val sincronizado: Boolean = false,
    val fecha_creacion_local: LocalDateTime = LocalDateTime.now() // Local timestamp
)
