package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.repository.UnidadProductivaRepository
import javax.inject.Inject

class GetUnidadesProductivasUseCase @Inject constructor(
    private val repository: UnidadProductivaRepository
) {
    suspend operator fun invoke(): Result<List<UnidadProductiva>> {
        return repository.getUnidadesProductivas()
    }
}
