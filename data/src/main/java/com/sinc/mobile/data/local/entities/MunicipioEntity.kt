package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "municipios")
data class MunicipioEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val latitud: Double?,
    val longitud: Double?,
    val geojson_boundary: String?
)
