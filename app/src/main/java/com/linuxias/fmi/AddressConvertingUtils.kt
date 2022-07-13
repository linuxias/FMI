package com.linuxias.fmi

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.widget.Toast
import java.io.IOException
import java.util.*

fun convertLocationToAddress(context: Context, location: Location): Address? {
    try {
        val addresses = Geocoder(context, Locale.getDefault())
            .getFromLocation(location.latitude, location.longitude, 1)
        return addresses.get(0)
    } catch (ioExcetption: IOException) {
        Toast.makeText(context, "Unable to use Geocoder",
            Toast.LENGTH_SHORT).show()
    } catch (illegalArgumentException: IllegalArgumentException) {
        Toast.makeText(context, "Wrong location information",
            Toast.LENGTH_SHORT).show()
    }

    return null
}