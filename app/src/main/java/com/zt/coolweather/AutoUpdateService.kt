package com.zt.coolweather

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import android.preference.PreferenceManager
import android.util.Log
import com.bumptech.glide.Glide
import com.zt.coolweather.util.handleWeatherResponse
import com.zt.coolweather.util.sendHttpRequest
import okhttp3.Call
import okhttp3.Response
import java.io.IOException

class AutoUpdateService : Service() {


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        updateWeather()
        updatePic()
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val anHour = 8 * 60 * 60 * 1000
        val updateTime = SystemClock.elapsedRealtime() + anHour
        val i = Intent(this,AutoUpdateService::class.java)
        val pi =  PendingIntent.getActivity(this,0,i,0)
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,updateTime,pi)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    fun updateWeather(): Unit {
        val prfs = PreferenceManager.getDefaultSharedPreferences(this)
        val weatherString = prfs.getString("weather", null)
        if (weatherString != null) {
            val weather = handleWeatherResponse(weatherString)
            weather?.let {
                val weatherId = it.basic.weatherId
                val url =
                    "http://guolin.tech/api/weather?cityid=${weatherId}&key=${WeatherActivity.KEY}"
                sendHttpRequest(url, object : okhttp3.Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseText = response.body?.string()
                        responseText?.let {
                            val weather1 = handleWeatherResponse(it)
                            weather1?.let {
                                if (weather1.status == "ok") {
                                    val edit =
                                        PreferenceManager.getDefaultSharedPreferences(this@AutoUpdateService)
                                            .edit()
                                    edit.putString("weather", responseText)
                                    edit.apply()
                                }

                            }
                        }
                    }

                })
            }
        }
    }

    fun updatePic(): Unit {
        val url = "http://guolin.tech/api/bing_pic"
        sendHttpRequest(url, object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("AutoUpdateService", "onFailure: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { it ->
                    val picUrl = it.string()
                    val edit =
                        PreferenceManager.getDefaultSharedPreferences(this@AutoUpdateService)
                            .edit()
                    edit.putString("bing_pic", picUrl)
                    edit.apply()
                }
            }

        })
    }
}
