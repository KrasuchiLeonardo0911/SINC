package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.GenericError
import com.sinc.mobile.domain.repository.CatalogosRepository
import com.sinc.mobile.domain.repository.UnidadProductivaRepository
import com.sinc.mobile.domain.util.Error
import com.sinc.mobile.domain.util.Result
import javax.inject.Inject

class SyncDataUseCase @Inject constructor(
    private val catalogosRepository: CatalogosRepository,
    private val unidadProductivaRepository: UnidadProductivaRepository
) {
    suspend operator fun invoke(): Result<Unit, Error> {
        val catalogosResult = catalogosRepository.syncCatalogos()
        if (catalogosResult is Result.Failure) {
            return Result.Failure(catalogosResult.error)
        }

        val unidadesResult = unidadProductivaRepository.syncUnidadesProductivas()
        if (unidadesResult is Result.Failure) {
            return Result.Failure(unidadesResult.error)
        }

        return Result.Success(Unit)
    }
}