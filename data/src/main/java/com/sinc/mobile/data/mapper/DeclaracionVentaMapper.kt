package com.sinc.mobile.data.mapper

import com.sinc.mobile.data.local.entities.DeclaracionVentaEntity
import com.sinc.mobile.data.network.dto.response.DeclaracionVentaDto
import com.sinc.mobile.domain.model.DeclaracionVenta

fun DeclaracionVentaDto.toEntity(): DeclaracionVentaEntity {
    return DeclaracionVentaEntity(
        id = id,
        productorId = productorId,
        unidadProductivaId = unidadProductivaId,
        especieId = especieId,
        razaId = razaId,
        categoriaAnimalId = categoriaAnimalId,
        cantidad = cantidad,
        estado = estado,
        fechaDeclaracion = fechaDeclaracion,
        observaciones = observaciones,
        pesoAproximadoKg = pesoAproximadoKg
    )
}

fun DeclaracionVentaEntity.toDomain(): DeclaracionVenta {
    return DeclaracionVenta(
        id = id,
        productorId = productorId,
        unidadProductivaId = unidadProductivaId,
        especieId = especieId,
        razaId = razaId,
        categoriaAnimalId = categoriaAnimalId,
        cantidad = cantidad,
        estado = estado,
        fechaDeclaracion = fechaDeclaracion,
        observaciones = observaciones,
        pesoAproximadoKg = pesoAproximadoKg
    )
}
