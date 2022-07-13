package com.linuxias.fmi

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.linuxias.fmi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var locationProvider: LocationProvider

    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
            }
            permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
            }
            else -> {
                ActivityCompat.requestPermissions(this@MainActivity,
                    REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!isLocationServicesAvailable()) {
            showDialogForLocationServicesSetting()
        }
        locationPermissionRequest.launch(REQUIRED_PERMISSIONS)

        locationProvider = LocationProvider(this)

        binding.btnRefresh.setOnClickListener {
            updateCurrentLocationAddress()
        }
    }

    fun updateCurrentLocationAddress() {
        locationProvider.receiveLocation {
            val address = convertLocationToAddress(this, it)
            address?.run {
                Log.d("TAG", "${address.countryName} ${address.locality}")
            }
        }
    }

    private fun showDialogForLocationServicesSetting() {
        val locationSettingLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK ||
                result.resultCode == Activity.RESULT_CANCELED) {
                if (!isLocationServicesAvailable()) {
                    Toast.makeText(this@MainActivity,
                        "You must turn on GPS service",
                        Toast.LENGTH_LONG)
                        .show()
                    finish()
                }
            }
        }

        AlertDialog.Builder(this@MainActivity)
            .setTitle("Would you want to enable GPS service?")
            .setPositiveButton("Open the setting app",
                DialogInterface.OnClickListener { _, _->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    locationSettingLauncher.launch(intent)
                })
            .setCancelable(false)
            .create()
            .show()
    }

    private fun isLocationServicesAvailable(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val isGpsAvailable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkAvailable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        return (isGpsAvailable || isNetworkAvailable)
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )

        private val PERMISSION_REQUEST_CODE = 100
    }
}