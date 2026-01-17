package com.sinc.mobile.domain.use_case.ventas

import com.sinc.mobile.domain.repository.VentasRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.model.GenericError as Error
import javax.inject.Inject

class CreateDeclaracionVentaUseCase @Inject constructor(
    private val repository: VentasRepository
) {
    suspend operator fun invoke(
        unidadProductivaId: Int,
        especieId: Int,
        razaId: Int,
        categoriaAnimalId: Int,
        cantidad: Int,
        observaciones: String?
    ): Result<Unit, Error> {
        return repository.createDeclaracion(
            unidadProductivaId,
            especieId,
            razaId,
            categoriaAnimalId,
            cantidad,
            observaciones
        )
    }
}
