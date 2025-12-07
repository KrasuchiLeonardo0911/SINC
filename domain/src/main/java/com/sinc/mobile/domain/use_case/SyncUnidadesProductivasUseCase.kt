package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.repository.UnidadProductivaRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import javax.inject.Inject

class SyncUnidadesProductivasUseCase @Inject constructor(
    private val repository: UnidadProductivaRepository
) {
    suspend operator fun invoke(): Result<Unit, Error> {
        return repository.syncUnidadesProductivas()
    }
}
