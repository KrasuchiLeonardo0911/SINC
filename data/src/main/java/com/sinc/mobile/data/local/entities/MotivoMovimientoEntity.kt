package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "motivo_movimientos")
data class MotivoMovimientoEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val tipo: String // "alta" or "baja"
)
