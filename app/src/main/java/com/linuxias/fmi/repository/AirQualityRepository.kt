package com.linuxias.fmi.repository

import androidx.annotation.WorkerThread
import retrofit2.Call

class AirQualityRepository(private val airQualityApi: AirQualityApi) {
    @Suppress
    @WorkerThread
    suspend fun getAirQuailityData(lat: String, lon: String, key: String): Call<AirQualityResponse> {
        return airQualityApi.getAirQuailityData(lat, lon, key)
    }
}