package com.zt.coolweather

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.zt.coolweather.json.Weather
import com.zt.coolweather.util.handleWeatherResponse
import com.zt.coolweather.util.sendHttpRequest
import okhttp3.Call
import okhttp3.Response
import java.io.IOException


class WeatherActivity : AppCompatActivity() {
    companion object {
        const val TAG = "WeatherActivity"
        const val KEY = "d732a53f967642a7ad191c991ddd3437"
    }

    private lateinit var weatherLayout: ScrollView
    lateinit var titleCity: TextView
    lateinit var titleUpdateTime: TextView
    lateinit var degreeText: TextView
    lateinit var weatherInfoText: TextView
    lateinit var forecastLayout: LinearLayout
    lateinit var aqiText: TextView
    lateinit var pm25Text: TextView
    lateinit var washCarText: TextView
    lateinit var comfortText: TextView
    lateinit var sportView: TextView
    val bingPiture: ImageView by lazy {
        findViewById<ImageView>(R.id.bing_pic)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun doSome() {
        window.setDecorFitsSystemWindows(false)
        val controller = window.insetsController
        if (controller != null) {
            controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
    @RequiresApi(22)
    fun doSome22(){
        val decorView = window.decorView
        decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN.or(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


//        if (Build.VERSION.SDK_INT>= 30){
//            doSome()
//        }else{
            doSome22()
//        }
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity_weather)
        weatherLayout = findViewById(R.id.weather_layout)
        titleCity = findViewById(R.id.title_city)
        titleUpdateTime = findViewById(R.id.title_update_time)
        degreeText = findViewById(R.id.degree_text)
        weatherInfoText = findViewById(R.id.weather_info_text)
        forecastLayout = findViewById(R.id.forecast_layout)
        aqiText = findViewById(R.id.aqi_text)
        pm25Text = findViewById(R.id.pm25_text)
        washCarText = findViewById(R.id.car_wash_text)
        comfortText = findViewById(R.id.conform_text)
        sportView = findViewById(R.id.sport_text)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val weatherString = prefs.getString("weather", null)
        if (weatherString != null) {
            val weather = handleWeatherResponse(weatherString)
            showWeatherInfo(weather)
        } else {
            val weatherID = intent.getStringExtra("weather_id")
            weatherLayout.visibility = View.INVISIBLE
            requestWeatherInfo(weatherID)
        }
        val picUrl = prefs.getString("bing_pic", null)
        if (picUrl != null) {
            Glide.with(this).load(picUrl).into(bingPiture)
        } else {
            loadImage()
        }
    }

    private fun loadImage() {
        val url = "http://guolin.tech/api/bing_pic"
        sendHttpRequest(url, object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "onFailure: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { it ->
                    val picUrl = it.string()
                    val edit =
                        PreferenceManager.getDefaultSharedPreferences(this@WeatherActivity)
                            .edit()
                    edit.putString("bing_pic", picUrl)
                    edit.apply()
                    runOnUiThread {
                        Glide.with(this@WeatherActivity).load(picUrl).into(bingPiture)
                    }
                }
            }

        })
    }

    private fun requestWeatherInfo(weatherID: String?) {
        val url = "http://guolin.tech/api/weather?cityid=${weatherID}&key=${KEY}"
        sendHttpRequest(url, object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "onFailure: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val weatherString = response.body?.string()
                weatherString?.let {
                    val weather = handleWeatherResponse(it)
                    weather?.let {
                        runOnUiThread {
                            if (weather.status == "ok") {
                                val edit =
                                    PreferenceManager.getDefaultSharedPreferences(this@WeatherActivity)
                                        .edit()
                                edit.putString("weather", weatherString)
                                edit.apply()
                                showWeatherInfo(weather)
                            } else {
                                Toast.makeText(this@WeatherActivity, "获取天气失败", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }

                    }
                }
                runOnUiThread {

                }
            }

        })
        loadImage()
    }

    private fun showWeatherInfo(weather: Weather?) {
        if (weather != null) {
            val cityName = weather.basic.cityName
            val updateTime = weather.basic.update.updateTime.split(" ")[0]
            val degree = "${weather.now.temperature}℃"
            val weatherInfo = weather.now.more.info
            titleCity.text = cityName
            titleUpdateTime.text = updateTime
            degreeText.text = degree
            weatherInfoText.text = weatherInfo
            forecastLayout.removeAllViews()
            for (forecast in weather.forecastList) {
                val view =
                    LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false)
                val dateText = view.findViewById<TextView>(R.id.date_text)
                val infoText = view.findViewById<TextView>(R.id.info_text)
                val minText = view.findViewById<TextView>(R.id.min_text)
                val maxText = view.findViewById<TextView>(R.id.max_text)
                dateText.text = forecast.date
                infoText.text = forecast.more.info
                minText.text = forecast.tempreture.min
                maxText.text = forecast.tempreture.max
                forecastLayout.addView(view)
            }
            aqiText.text = weather.aqi.city.aqi
            pm25Text.text = weather.aqi.city.pm25
            comfortText.text = "舒适度：${weather.suggestion.comfort.info}"
            washCarText.text = "洗车指数：${weather.suggestion.carWash.info}"
            sportView.text = "运动指数：${weather.suggestion.sport.info}"
            weatherLayout.visibility = View.VISIBLE

        }
    }
}
