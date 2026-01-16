package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.model.UpdateUnidadProductivaData
import com.sinc.mobile.domain.repository.UnidadProductivaRepository
import com.sinc.mobile.domain.util.Error
import com.sinc.mobile.domain.util.Result
import javax.inject.Inject

class UpdateUnidadProductivaUseCase @Inject constructor(
    private val repository: UnidadProductivaRepository
) {
    suspend operator fun invoke(id: Int, data: UpdateUnidadProductivaData): Result<UnidadProductiva, Error> {
        return repository.updateUnidadProductiva(id, data)
    }
}
