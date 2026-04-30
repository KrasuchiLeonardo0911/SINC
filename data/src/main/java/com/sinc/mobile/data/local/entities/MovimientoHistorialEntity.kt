package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "movimiento_historial",
    indices = [Index(value = ["id"], unique = true)]
)
data class MovimientoHistorialEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val id: Long? = null,
    val fechaRegistro: LocalDateTime,
    val cantidad: Int,
    val especie: String,
    val especieId: Int,
    val categoria: String,
    val categoriaId: Int,
    val raza: String,
    val razaId: Int,
    val motivo: String,
    val motivoId: Int,
    val tipoMovimiento: String,
    val unidadProductiva: String,
    val unidadProductivaId: Int,
    val destinoTraslado: String?,
    val sincronizado: Boolean = false
)
