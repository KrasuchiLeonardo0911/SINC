package com.sinc.mobile.domain.use_case.init

import com.sinc.mobile.domain.model.InitData
import com.sinc.mobile.domain.repository.AuthRepository
import com.sinc.mobile.domain.use_case.SyncCatalogosUseCase
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import javax.inject.Inject

class InitializeAppUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncCatalogosUseCase: SyncCatalogosUseCase
) {
    suspend operator fun invoke(): Result<InitData, Error> {
        val initResult = authRepository.getInitData()

        if (initResult is Result.Success) {
            val initData = initResult.data
            // Pasamos la versión remota al caso de uso de sincronización.
            // El repositorio se encargará de comparar con la local y decidir si sincronizar.
            val remoteVersion = initData.configuration?.catalogsVersion
            syncCatalogosUseCase(remoteVersion)
        }

        return initResult
    }
}
