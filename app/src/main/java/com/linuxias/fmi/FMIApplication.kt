package com.linuxias.fmi

import android.app.Application
import android.util.Log
import com.linuxias.fmi.repository.AirQualityConnection
import com.linuxias.fmi.repository.AirQualityRepository
import com.linuxias.fmi.repository.AirQualityApi

class FMIApplication : Application() {
    companion object {
        var repository: AirQualityRepository? = null
        var api: AirQualityApi? = null
    }

    override fun onCreate() {
        super.onCreate()
        api = AirQualityConnection.getService()
        repository = AirQualityRepository(api!!)
    }
}

const val TAG = "FMI"