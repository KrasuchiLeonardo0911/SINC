package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuentes_agua")
data class FuenteAguaEntity(
    @PrimaryKey val id: Int,
    val nombre: String
)
