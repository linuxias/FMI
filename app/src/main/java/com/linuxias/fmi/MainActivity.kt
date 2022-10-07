package com.linuxias.fmi

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.location.Address
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.*
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
    private var address: Address? = null

    private val startMapActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
        object: ActivityResultCallback<ActivityResult>{
            override fun onActivityResult(result: ActivityResult?) {
                if ((result?.resultCode ?: 0) == Activity.RESULT_OK) {
                    val lat = result?.data?.getDoubleExtra("latitude", 0.0) ?: 0.0
                    val long = result?.data?.getDoubleExtra("longitude", 0.0) ?: 0.0

                    updateSpecificLocationAndTime(lat, long)
                }
            }
        })

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
        updateCurrentLocationAndTime()

        val viewModelFactory = AirQualityViewModelFactory(FMIApplication.repository!!)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(AirQualityViewModel::class.java)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.airQualityResponse.collect {
                    if (it != null) {
                        binding.tvCount.text = it.data.current.pollution.aqius.toString()
                    }
                }
            }
        }

        binding.btnRefresh.setOnClickListener {
            updateCurrentLocationAndTime()
        }

        binding.mapBtn.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("currentLatitude", address!!.latitude)
            intent.putExtra("currentLongitude", address!!.longitude)
            startMapActivityResult.launch(intent)
        }
    }

    fun updateSpecificLocationAndTime(latitude: Double, longitude: Double) {
        address = convertGeoToAddress(this, latitude, longitude)
        address?.let {
            binding.locationTitle.text = "${it.countryName}"
            binding.locationSubtitle.text = it.locality.toString() + " " + it.thoroughfare.toString()
            viewModel.getAirQualityData(it.latitude.toString(), it.longitude.toString(), getApiKey())
        }
    }

    fun updateCurrentLocationAndTime() {
        locationProvider.receiveLocation {
            address = convertLocationToAddress(this, it)
            address?.let {
                binding.locationTitle.text = "${it.countryName}"
                binding.locationSubtitle.text = it.locality.toString() + " " + it.thoroughfare.toString()
                viewModel.getAirQualityData(it.latitude.toString(), it.longitude.toString(), getApiKey())
            }
        }

        val date = Date(System.currentTimeMillis())
        binding.tvCheckTime.text = SimpleDateFormat("hh:mm:ss", Locale.KOREAN).format(date)
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