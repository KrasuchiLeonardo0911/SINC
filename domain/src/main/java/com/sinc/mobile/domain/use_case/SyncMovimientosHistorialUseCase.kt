package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.GenericError
import com.sinc.mobile.domain.repository.MovimientoHistorialRepository
import com.sinc.mobile.domain.util.Result
import javax.inject.Inject

class SyncMovimientosHistorialUseCase @Inject constructor(
    private val repository: MovimientoHistorialRepository
) {
    suspend operator fun invoke(): Result<Unit, GenericError> {
        return repository.syncMovimientos()
    }
}
