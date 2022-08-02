package com.linuxias.fmi

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.linuxias.fmi.databinding.ActivityMainBinding
import com.linuxias.fmi.repository.AirQualityViewModel
import com.linuxias.fmi.repository.AirQualityViewModelFactory
import com.linuxias.fmi.utils.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var locationProvider: LocationProvider
    private lateinit var viewModel: AirQualityViewModel

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
        updateCurrentLocationAddress()

        val viewModelFactory = AirQualityViewModelFactory(FMIApplication.repository!!)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(AirQualityViewModel::class.java)

        viewModel.viewModelScope.launch {
            viewModel.airQualityResponse.collect {
                if (it != null) {
                    binding.tvCount.text = it.data.current.pollution.aqius.toString()
                    val date = Date(System.currentTimeMillis())
                    binding.tvCheckTime.text = SimpleDateFormat("hh:mm:ss", Locale.KOREAN).format(date)
                }
            }
        }

        binding.btnRefresh.setOnClickListener {
            updateCurrentLocationAddress()
        }
    }

    fun updateCurrentLocationAddress() {
        locationProvider.receiveLocation {
            val address = convertLocationToAddress(this, it)
            address?.let {
                binding.locationTitle.text = "${it.countryName}"
                binding.locationSubtitle.text = it.locality.toString() + " " + it.thoroughfare.toString()
                viewModel.getAirQualityData(it.latitude.toString(), it.longitude.toString(), getApiKey())
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