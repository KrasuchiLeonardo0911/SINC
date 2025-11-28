package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.Location
import com.sinc.mobile.domain.model.LocationError
import com.sinc.mobile.domain.util.Result

interface LocationRepository {
    suspend fun getCurrentLocation(): Result<Location, LocationError>
}
