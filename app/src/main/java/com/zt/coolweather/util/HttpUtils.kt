package com.zt.coolweather.util

import okhttp3.OkHttpClient
import okhttp3.Request

fun sendHttpRequest(url: String, callback: okhttp3.Callback) {
    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()
    client.newCall(request).enqueue(callback)
}
