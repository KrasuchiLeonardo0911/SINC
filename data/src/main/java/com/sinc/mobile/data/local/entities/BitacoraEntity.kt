package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "bitacoras")
data class BitacoraEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val fecha: LocalDateTime,
    val contenido: String,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)
