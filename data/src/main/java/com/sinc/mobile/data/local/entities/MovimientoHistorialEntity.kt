package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "movimiento_historial")
data class MovimientoHistorialEntity(
    @PrimaryKey
    val id: Long,
    val fechaRegistro: LocalDateTime,
    val cantidad: Int,
    val especie: String,
    val categoria: String,
    val raza: String,
    val motivo: String,
    val tipoMovimiento: String,
    val unidadProductiva: String,
    val destinoTraslado: String?
)
