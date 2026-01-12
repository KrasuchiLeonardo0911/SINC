package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.MovimientoPendiente
import com.sinc.mobile.domain.repository.MovimientoRepository
import javax.inject.Inject

class SyncMovimientosPendientesUseCase @Inject constructor(
    private val repository: MovimientoRepository
) {
    suspend operator fun invoke(): Result<List<MovimientoPendiente>> {
        return repository.syncMovimientosPendientes()
    }
}
