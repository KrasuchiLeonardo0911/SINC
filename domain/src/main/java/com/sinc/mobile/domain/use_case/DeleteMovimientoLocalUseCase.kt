package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.MovimientoPendiente
import com.sinc.mobile.domain.repository.MovimientoRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import javax.inject.Inject

class DeleteMovimientoLocalUseCase @Inject constructor(
    private val repository: MovimientoRepository
) {
    suspend operator fun invoke(movimiento: MovimientoPendiente): Result<Unit, Error> {
        return repository.deleteMovimientoLocal(movimiento)
    }
}