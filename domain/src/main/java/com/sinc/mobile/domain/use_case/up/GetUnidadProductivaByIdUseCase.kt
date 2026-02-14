package com.sinc.mobile.domain.use_case.up

import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.repository.UnidadProductivaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUnidadProductivaByIdUseCase @Inject constructor(
    private val repository: UnidadProductivaRepository
) {
    operator fun invoke(id: Int): Flow<UnidadProductiva?> {
        return repository.getUnidadProductivaById(id)
    }
}

