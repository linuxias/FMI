package com.linuxias.fmi

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import java.lang.Exception

class LocationProvider(val context: Context) {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    init {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    }

    fun receiveLocation(block: (location: Location) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                block(location)
            }
    }
}