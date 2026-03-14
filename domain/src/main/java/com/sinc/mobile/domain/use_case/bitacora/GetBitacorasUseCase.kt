package com.sinc.mobile.domain.use_case.bitacora

import com.sinc.mobile.domain.model.Bitacora
import com.sinc.mobile.domain.repository.BitacoraRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBitacorasUseCase @Inject constructor(
    private val repository: BitacoraRepository
) {
    operator fun invoke(): Flow<List<Bitacora>> {
        return repository.getBitacoras()
    }
}
