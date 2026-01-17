package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "declaraciones_venta")
data class DeclaracionVentaEntity(
    @PrimaryKey
    val id: Int,
    val productorId: Int,
    val unidadProductivaId: Int,
    val especieId: Int,
    val razaId: Int,
    val categoriaAnimalId: Int,
    val cantidad: Int,
    val estado: String,
    val fechaDeclaracion: String,
    val observaciones: String?
)
