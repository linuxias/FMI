package com.linuxias.fmi.repository

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AirQualityConnection {
    companion object {
        private const val BASE_URL = "https://api.airvisual.com/v2/"
        @Volatile
        private var INSTANCE: AirQualityApi? = null

        fun getService(): AirQualityApi? {
            return INSTANCE ?: synchronized(this) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                INSTANCE = retrofit.create(AirQualityApi::class.java)
                INSTANCE
            }
        }
    }
}