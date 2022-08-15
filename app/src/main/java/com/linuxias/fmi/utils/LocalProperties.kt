package com.linuxias.fmi.utils

import com.linuxias.fmi.BuildConfig

fun getApiKey(): String {
    return BuildConfig.AQ_API_KEY
}