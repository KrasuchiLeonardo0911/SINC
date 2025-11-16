package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.repository.UnidadProductivaRepository
import javax.inject.Inject

class SyncUnidadesProductivasUseCase @Inject constructor(
    private val repository: UnidadProductivaRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.syncUnidadesProductivas()
    }
}
