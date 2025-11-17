package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "condiciones_tenencia")
data class CondicionTenenciaEntity(
    @PrimaryKey val id: Int,
    val nombre: String
)
