package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.repository.LocationRepository
import javax.inject.Inject

class GetCurrentLocationUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke() = repository.getCurrentLocation()
}
