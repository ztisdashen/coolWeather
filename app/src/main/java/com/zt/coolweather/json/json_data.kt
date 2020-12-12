package com.zt.coolweather.json

import com.google.gson.annotations.SerializedName

data class Basic(
    @SerializedName("city")
    var cityName: String,
    @SerializedName("id")
    var weatherId: String,
    var update: Update

)

data class Update(
    @SerializedName("loc")
    var updateTime: String
)

data class AQI(
    var city: AQICity
) {
    data class AQICity(
        var aqi: String,
        var pm25: String
    )
}

data class Now(
    @SerializedName("tmp")
    var temperature: String,
    @SerializedName("cond")
    var more: More
) {
    data class More(
        @SerializedName("txt")
        var info: String
    )
}

data class Suggestion(
    @SerializedName("comf")
    var comfort: Comfort,
    @SerializedName("cw")
    var carWash: CarWash,
    var sport: Sport
) {
    data class Comfort(
        @SerializedName("txt")
        var info: String
    )

    data class CarWash(
        @SerializedName("txt")
        var info: String
    )

    data class Sport(
        @SerializedName("txt")
        var info: String
    )
}

data class Forecast(
    var date: String,
    @SerializedName("tmp")
    var tempreture: Temperature,
    @SerializedName("cond")
    var more: More
) {
    data class Temperature(var max: String, var min: String)
    data class More(
        @SerializedName("txt_d")
        var info: String
    )
}

data class Weather(
    var status: String,
    var basic: Basic,
    var aqi: AQI,
    var now: Now,
    var suggestion: Suggestion,
    @SerializedName("daily_forecast")
    var forecastList: MutableList<Forecast>

)
