package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.repository.CatalogosRepository
import com.sinc.mobile.domain.repository.UnidadProductivaRepository
import javax.inject.Inject

class SyncDataUseCase @Inject constructor(
    private val catalogosRepository: CatalogosRepository,
    private val unidadProductivaRepository: UnidadProductivaRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        val catalogosResult = catalogosRepository.syncCatalogos()
        if (catalogosResult.isFailure) {
            return Result.failure(catalogosResult.exceptionOrNull() ?: Exception("Error desconocido al sincronizar catálogos"))
        }

        val unidadesResult = unidadProductivaRepository.syncUnidadesProductivas()
        if (unidadesResult.isFailure) {
            return Result.failure(unidadesResult.exceptionOrNull() ?: Exception("Error desconocido al sincronizar unidades productivas"))
        }

        return Result.success(Unit)
    }
}
