/*
package com.sinc.mobile.data.local.entities.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.sinc.mobile.data.local.entities.TipoPastoEntity
import com.sinc.mobile.data.local.entities.TipoSueloEntity
import com.sinc.mobile.data.local.entities.UnidadProductivaEntity
import com.sinc.mobile.data.local.entities.UnidadProductivaTipoPastoCrossRef
import com.sinc.mobile.data.local.entities.UnidadProductivaTipoSueloCrossRef


data class UnidadProductivaWithDetails(
    @Embedded val unidadProductiva: UnidadProductivaEntity,

    @Relation(
        parentColumn = "id", // id de UnidadProductivaEntity
        entity = TipoSueloEntity::class,
        associateBy = Junction(
            value = UnidadProductivaTipoSueloCrossRef::class,
            parentColumn = "unidadProductivaId", // Columna en CrossRef que apunta a UnidadProductivaEntity
            entityColumn = "tipoSueloId"         // Columna en CrossRef que apunta a TipoSueloEntity
        )
    )
    val tiposSuelo: List<TipoSueloEntity>,

    @Relation(
        parentColumn = "id", // id de UnidadProductivaEntity
        entity = TipoPastoEntity::class,
        associateBy = Junction(
            value = UnidadProductivaTipoPastoCrossRef::class,
            parentColumn = "unidadProductivaId", // Columna en CrossRef que apunta a UnidadProductivaEntity
            entityColumn = "tipoPastoId"         // Columna en CrossRef que apunta a TipoPastoEntity
        )
    )
    val tiposPasto: List<TipoPastoEntity>
)
*/
