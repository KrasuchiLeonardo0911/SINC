package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.repository.CatalogosRepository
import javax.inject.Inject

class SyncCatalogosUseCase @Inject constructor(
    private val repository: CatalogosRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.syncCatalogos()
    }
}
