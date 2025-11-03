package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.repository.UnidadProductivaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUnidadesProductivasUseCase @Inject constructor(
    private val repository: UnidadProductivaRepository
) {
    operator fun invoke(): Flow<List<UnidadProductiva>> {
        return repository.getUnidadesProductivas()
    }
}
