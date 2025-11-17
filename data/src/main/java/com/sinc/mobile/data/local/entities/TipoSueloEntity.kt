package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tipos_suelo")
data class TipoSueloEntity(
    @PrimaryKey val id: Int,
    val nombre: String
)
