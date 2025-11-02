package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "especies")
data class EspecieEntity(
    @PrimaryKey val id: Int,
    val nombre: String
)
