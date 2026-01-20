package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.MovimientoPendiente
import com.sinc.mobile.domain.repository.MovimientoRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import javax.inject.Inject

class SyncMovimientosLocalesUseCase @Inject constructor(
    private val repository: MovimientoRepository
) {
    suspend operator fun invoke(): Result<List<MovimientoPendiente>, Error> {
        return repository.syncMovimientosPendientesToServer()
    }
}