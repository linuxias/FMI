package com.linuxias.fmi

import android.app.Activity
import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.linuxias.fmi.databinding.ActivityMainBinding
import com.linuxias.fmi.databinding.ActivityMapBinding
import com.linuxias.fmi.utils.getApiKey

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapBinding
    private var latitude = 0.0
    private var longitude = 0.0

    private var map: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        latitude = intent.getDoubleExtra("currentLatitude", 0.0)
        longitude = intent.getDoubleExtra("currentLongitude", 0.0)

        binding.checkHereBtn.setOnClickListener {
            map?.let {
                val intent = Intent()
                intent.putExtra("latitude", it.cameraPosition.target.latitude)
                intent.putExtra("longitude", it.cameraPosition.target.longitude)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map?.let {
            val lntLng = LatLng(latitude, longitude)
            it.setMaxZoomPreference(20.0f)
            it.setMinZoomPreference(12.0f)
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(lntLng, 16f))
        }

        setMarker()

        binding.currentLocationBtn.setOnClickListener {
            val locationProvider = LocationProvider(this@MapActivity)
            locationProvider.receiveLocation {
                val address = convertLocationToAddress(this, it)
                address?.let {
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.latitude, it.longitude),
                        16f))
                }
            }
        }
    }

    private fun setMarker() {
        map?.let {
            it.clear()
            val marker = it.addMarker(
                MarkerOptions().position(it.cameraPosition.target)
                    .title("Marker Position")
            )

            it.setOnCameraMoveListener {
                marker?.let { marker->
                    marker.setPosition(it.cameraPosition.target)
                }
            }
        }
    }

}