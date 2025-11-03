package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.Catalogos
import com.sinc.mobile.domain.repository.CatalogosRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCatalogosUseCase @Inject constructor(
    private val repository: CatalogosRepository
) {
    operator fun invoke(): Flow<Catalogos> {
        return repository.getCatalogos()
    }
}
