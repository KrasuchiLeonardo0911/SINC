package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unidades_productivas")
data class UnidadProductivaEntity(
    @PrimaryKey val id: Int,
    val nombre: String?,
    val identificador_local: String? = null,
    val latitud: Double?,
    val longitud: Double?,
    val municipio_id: Int?,
    val paraje_id: Int?,
    val activo: Boolean = false,
    val completo: Boolean = false
)
