package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "unidad_productiva_tipo_suelo",
    primaryKeys = ["unidadProductivaId", "tipoSueloId"],
    foreignKeys = [
        ForeignKey(
            entity = UnidadProductivaEntity::class,
            parentColumns = ["id"],
            childColumns = ["unidadProductivaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TipoSueloEntity::class,
            parentColumns = ["id"],
            childColumns = ["tipoSueloId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ]
)
data class UnidadProductivaTipoSueloCrossRef(
    val unidadProductivaId: Int,
    val tipoSueloId: Int,
    val porcentaje: Int
)
