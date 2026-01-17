package com.sinc.mobile.domain.use_case.ventas

import com.sinc.mobile.domain.repository.VentasRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.model.GenericError as Error
import javax.inject.Inject

class SyncDeclaracionesVentaUseCase @Inject constructor(
    private val repository: VentasRepository
) {
    suspend operator fun invoke(): Result<Unit, Error> {
        return repository.syncDeclaraciones()
    }
}
