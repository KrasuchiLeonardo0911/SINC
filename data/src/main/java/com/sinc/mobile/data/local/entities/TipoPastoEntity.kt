package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tipos_pasto")
data class TipoPastoEntity(
    @PrimaryKey val id: Int,
    val nombre: String
)
