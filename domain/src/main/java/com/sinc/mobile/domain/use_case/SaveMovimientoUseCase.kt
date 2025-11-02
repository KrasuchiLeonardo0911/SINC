package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.Movimiento
import com.sinc.mobile.domain.repository.MovimientoRepository
import javax.inject.Inject

class SaveMovimientoUseCase @Inject constructor(
    private val repository: MovimientoRepository
) {
    suspend operator fun invoke(movimiento: Movimiento): Result<Unit> {
        return repository.saveMovimiento(movimiento)
    }
}
