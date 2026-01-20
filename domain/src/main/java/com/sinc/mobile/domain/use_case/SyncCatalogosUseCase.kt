package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.repository.CatalogosRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import javax.inject.Inject

class SyncCatalogosUseCase @Inject constructor(
    private val repository: CatalogosRepository
) {
    suspend operator fun invoke(remoteVersion: String? = null): Result<Unit, Error> {
        return repository.syncCatalogos(remoteVersion)
    }
}
