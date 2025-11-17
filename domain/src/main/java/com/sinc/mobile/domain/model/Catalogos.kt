package com.sinc.mobile.domain.model

data class Catalogos(
    val especies: List<Especie>,
    val razas: List<Raza>,
    val categorias: List<Categoria>,
    val motivosMovimiento: List<MotivoMovimiento>,
    val municipios: List<Municipio>,
    val condicionesTenencia: List<CondicionTenencia>,
    val fuentesAgua: List<FuenteAgua>,
    val tiposSuelo: List<TipoSuelo>,
    val tiposPasto: List<TipoPasto>
)