package com.zt.coolweather.util

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.zt.coolweather.db.City
import com.zt.coolweather.db.County
import com.zt.coolweather.db.Province
import com.zt.coolweather.json.Weather
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

/**
 * @param response http返回的省份数据
 * @return
 */
fun handleProvinceResponse(response: String): Boolean {
    if (response.isNotBlank()) {
        try {
            val provincesJSON = JSONArray(response)
            for (i in 0 until provincesJSON.length()) {
                val provinceJSON = provincesJSON.getJSONObject(i)
                val province =
                    Province(
                        null,
                        provinceJSON.getString("name"),
                        provinceJSON.getInt("id")
                    )
                province.save()
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return false
}

/**
 * @param response http返回的省份数据
 * @return
 */
fun handleCityResponse(response: String, provinceId: Int): Boolean {
    if (response.isNotBlank()) {
        try {
            val citiesJSON = JSONArray(response)
            for (i in 0 until citiesJSON.length()) {
                val cityJSON = citiesJSON.getJSONObject(i)
                val city =
                    City(
                        null,
                        cityJSON.getString("name"),
                        cityJSON.getInt("id"),
                        provinceId
                    )
                city.save()
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return false
}

/**
 * @param response http返回的省份数据
 * @return
 */
fun handleCountyResponse(response: String, cityId: Int): Boolean {
    if (response.isNotBlank()) {
        try {
            val countiesJSON = JSONArray(response)
            for (i in 0 until countiesJSON.length()) {
                val countyJSON = countiesJSON.getJSONObject(i)
                val county =
                    County(
                        null,
                        countyJSON.getString("name"),
                        countyJSON.getString("weather_id"),
                        cityId = cityId
                    )
                county.save()
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return false
}

fun handleWeatherResponse(response: String): Weather? {
    try {
        val jsonObj = JSONObject(response)
        val jsonArray = jsonObj.getJSONArray("HeWeather")
        val weatherContent = jsonArray.getJSONObject(0).toString()
        return Gson().fromJson(weatherContent,Weather::class.java)
    }catch (e:Exception){
        e.printStackTrace()
    }
    return null;
}
