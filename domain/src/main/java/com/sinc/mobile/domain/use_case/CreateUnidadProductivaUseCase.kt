package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.CreateUnidadProductivaData
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.repository.UnidadProductivaRepository
import javax.inject.Inject

class CreateUnidadProductivaUseCase @Inject constructor(
    private val repository: UnidadProductivaRepository
) {
    suspend operator fun invoke(data: CreateUnidadProductivaData): Result<UnidadProductiva> {
        // Aquí se podrían añadir validaciones de negocio adicionales antes de llamar al repositorio
        return repository.createUnidadProductiva(data)
    }
}
