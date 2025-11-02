package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.Catalogos
import com.sinc.mobile.domain.repository.CatalogosRepository
import javax.inject.Inject

class GetCatalogosUseCase @Inject constructor(
    private val repository: CatalogosRepository
) {
    suspend operator fun invoke(): Result<Catalogos> {
        return repository.getCatalogos()
    }
}
