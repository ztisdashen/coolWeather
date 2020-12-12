package com.zt.coolweather.db

import org.litepal.crud.LitePalSupport

data class Province(var id: Int?, var provinceName: String?, var provinceCode: Int?) : LitePalSupport()
data class City(var id: Int?, var cityName: String?, var cityCode: Int?, var provinceId: Int?) :
    LitePalSupport()

/**
 *
 */
data class County(var id: Int?, var countyName: String?, var weatherId: String?, var cityId: Int?) :
    LitePalSupport()

