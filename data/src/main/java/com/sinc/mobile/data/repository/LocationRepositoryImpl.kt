package com.sinc.mobile.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.sinc.mobile.domain.model.Location
import com.sinc.mobile.domain.model.LocationError
import com.sinc.mobile.domain.repository.LocationRepository
import com.sinc.mobile.domain.util.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class LocationRepositoryImpl @Inject constructor(
    private val locationClient: FusedLocationProviderClient,
    @ApplicationContext private val context: Context
) : LocationRepository {

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Result<Location, LocationError> {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            return Result.Failure(LocationError.NoPermission)
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled) {
            return Result.Failure(LocationError.GpsDisabled)
        }

        return suspendCancellableCoroutine { continuation ->
            locationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    continuation.resume(
                        Result.Success(
                            Location(
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        )
                    )
                } else {
                    // If lastLocation is null, you might want to request a fresh location.
                    // For simplicity here, we return an error.
                    continuation.resume(Result.Failure(LocationError.LocationNotFound))
                }
            }.addOnFailureListener {
                continuation.resume(Result.Failure(LocationError.UnknownError))
            }.addOnCanceledListener {
                continuation.cancel()
            }
        }
    }
}
