package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.MovimientoPendiente
import com.sinc.mobile.domain.repository.MovimientoRepository
import javax.inject.Inject

class DeleteMovimientoLocalUseCase @Inject constructor(
    private val repository: MovimientoRepository
) {
    suspend operator fun invoke(movimiento: MovimientoPendiente): Result<Unit> {
        return repository.deleteMovimientoLocal(movimiento)
    }
}
