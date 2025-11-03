package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.MovimientoPendiente
import com.sinc.mobile.domain.repository.MovimientoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMovimientosPendientesUseCase @Inject constructor(
    private val repository: MovimientoRepository
) {
    operator fun invoke(): Flow<List<MovimientoPendiente>> {
        return repository.getMovimientosPendientes()
    }
}
