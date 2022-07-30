package com.linuxias.fmi.repository

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.linuxias.fmi.MainActivity
import com.linuxias.fmi.TAG
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AirQualityViewModel(private val repository: AirQualityRepository) : ViewModel() {
    val airQualityResponse: MutableStateFlow<AirQualityResponse?> = MutableStateFlow(null)

    fun getAirQualityData(lat: String, lon: String, key: String) {
        viewModelScope.launch {
            val response = repository.getAirQuailityData(lat, lon, key)
            response.enqueue(object : Callback<AirQualityResponse> {
                override fun onResponse(
                    call: Call<AirQualityResponse>,
                    response: Response<AirQualityResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "Get AirQuality data successfully")
                        airQualityResponse.value = response.body()!!
                    }
                }

                override fun onFailure(call: Call<AirQualityResponse>, t: Throwable) {
                    t.printStackTrace()
                }
            })
        }
    }
}

class AirQualityViewModelFactory(private val repository: AirQualityRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AirQualityViewModel::class.java)) {
            return AirQualityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}