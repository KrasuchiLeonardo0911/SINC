package com.sinc.mobile.domain.model

data class UnidadProductiva(
    val id: Int,
    val nombre: String?,
    val identificadorLocal: String?,
    val superficie: Float?,
    val latitud: Double?,
    val longitud: Double?,
    val municipioId: Int?,
    val condicionTenenciaId: Int?,
    // Land Data
    val aguaHumanoFuenteId: Int?,
    val aguaHumanoEnCasa: Boolean?,
    val aguaHumanoDistancia: Int?,
    val aguaAnimalFuenteId: Int?,
    val aguaAnimalDistancia: Int?,
    val tipoSueloId: Int?,
    val tipoPastoId: Int?,
    val forrajerasPredominante: Boolean?,
    val habita: Boolean?,
    val observaciones: String? = null
)