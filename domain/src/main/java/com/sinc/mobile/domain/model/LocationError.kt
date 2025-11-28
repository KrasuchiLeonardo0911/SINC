package com.sinc.mobile.domain.model

import com.sinc.mobile.domain.util.Error

sealed class LocationError : Error {
    data object NoPermission : LocationError() // This represents when permission was not checked, not that it was denied.
    data object PermissionDenied : LocationError() // This represents when the user explicitly denies the permission.
    data object GpsDisabled : LocationError()
    data object LocationNotFound : LocationError()
    data object UnknownError : LocationError()
}
