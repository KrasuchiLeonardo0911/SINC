package com.sinc.mobile.domain.use_case.bitacora

import com.sinc.mobile.domain.repository.BitacoraRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import javax.inject.Inject

class DeleteBitacoraUseCase @Inject constructor(
    private val repository: BitacoraRepository
) {
    suspend operator fun invoke(id: Int): Result<Unit, Error> {
        return repository.deleteBitacora(id)
    }
}
