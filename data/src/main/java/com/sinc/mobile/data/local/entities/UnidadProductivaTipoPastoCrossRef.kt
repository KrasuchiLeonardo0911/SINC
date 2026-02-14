package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "unidad_productiva_tipo_pasto",
    primaryKeys = ["unidadProductivaId", "tipoPastoId"],
    foreignKeys = [
        ForeignKey(
            entity = UnidadProductivaEntity::class,
            parentColumns = ["id"],
            childColumns = ["unidadProductivaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TipoPastoEntity::class,
            parentColumns = ["id"],
            childColumns = ["tipoPastoId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ]
)
data class UnidadProductivaTipoPastoCrossRef(
    val unidadProductivaId: Int,
    val tipoPastoId: Int,
    val porcentaje: Int?
)
