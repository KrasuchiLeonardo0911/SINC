package com.sinc.mobile.domain.model

data class UpdateUnidadProductivaData(
    val superficie: Double? = null,
    val condicionTenenciaId: Int? = null,
    val aguaAnimalFuenteId: Int? = null,
    val aguaHumanoFuenteId: Int? = null,
    val aguaHumanoEnCasa: Boolean? = null,
    val aguaHumanoDistancia: Int? = null,
    val aguaAnimalDistancia: Int? = null,
    val tipoSueloId: Int? = null,
    val tipoPastoId: Int? = null,
    val forrajerasPredominante: Boolean? = null,
    val habita: Boolean? = null,
    val observaciones: String? = null
)
