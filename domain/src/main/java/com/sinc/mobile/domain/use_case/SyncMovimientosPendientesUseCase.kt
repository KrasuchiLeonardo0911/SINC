package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.repository.MovimientoRepository
import javax.inject.Inject

class SyncMovimientosPendientesUseCase @Inject constructor(
    private val repository: MovimientoRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.syncMovimientosPendientes()
    }
}
