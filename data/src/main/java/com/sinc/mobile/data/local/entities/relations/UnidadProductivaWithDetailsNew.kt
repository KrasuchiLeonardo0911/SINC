package com.sinc.mobile.data.local.entities.relations

import com.sinc.mobile.data.local.entities.TipoPastoEntity
import com.sinc.mobile.data.local.entities.TipoSueloEntity
import com.sinc.mobile.data.local.entities.UnidadProductivaEntity

data class SueloWithPercentage(
    val tipoSuelo: TipoSueloEntity,
    val porcentaje: Int
)

data class PastoWithPercentage(
    val tipoPasto: TipoPastoEntity,
    val porcentaje: Int?
)

data class UnidadProductivaWithDetailsNew(
    val unidadProductiva: UnidadProductivaEntity,
    val suelos: List<SueloWithPercentage>,
    val pastos: List<PastoWithPercentage>
)
