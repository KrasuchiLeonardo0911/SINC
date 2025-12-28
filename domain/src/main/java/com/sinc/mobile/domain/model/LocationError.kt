package com.sinc.mobile.domain.model

import com.sinc.mobile.domain.util.Error

sealed class LocationError : Error {
    data object NoPermission : LocationError() { override val message: String = "Location permission was not checked or is not available." }
    data object PermissionDenied : LocationError() { override val message: String = "Location permission denied by user." }
    data object GpsDisabled : LocationError() { override val message: String = "GPS is disabled." }
    data object LocationNotFound : LocationError() { override val message: String = "Location not found." }
    data object UnknownError : LocationError() { override val message: String = "An unknown location error occurred." }
}
