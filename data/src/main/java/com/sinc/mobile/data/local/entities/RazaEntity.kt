package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "razas",
    foreignKeys = [
        ForeignKey(
            entity = EspecieEntity::class,
            parentColumns = ["id"],
            childColumns = ["especie_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RazaEntity(
    @PrimaryKey val id: Int,
    val especie_id: Int,
    val nombre: String
)
