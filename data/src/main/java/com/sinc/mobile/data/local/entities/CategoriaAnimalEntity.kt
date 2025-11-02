package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "categoria_animals",
    foreignKeys = [
        ForeignKey(
            entity = EspecieEntity::class,
            parentColumns = ["id"],
            childColumns = ["especie_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CategoriaAnimalEntity(
    @PrimaryKey val id: Int,
    val especie_id: Int,
    val nombre: String
)
