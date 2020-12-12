package com.zt.coolweather

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.zt.coolweather.db.City
import com.zt.coolweather.db.County
import com.zt.coolweather.db.Province
import com.zt.coolweather.util.handleCityResponse
import com.zt.coolweather.util.handleCountyResponse
import com.zt.coolweather.util.handleProvinceResponse
import com.zt.coolweather.util.sendHttpRequest
import okhttp3.Call
import okhttp3.Response
import org.litepal.LitePal
import org.litepal.crud.DataSupport
import org.litepal.crud.LitePalSupport
import java.io.IOException
import java.util.logging.Level
import java.util.zip.Inflater

/**
 * 遍历省市县数据的fragment
 *
 * d732a53f967642a7ad191c991ddd3437
 */
class ChooseAreaFragment : Fragment() {
    var progressDialog: ProgressDialog? = null
    lateinit var titleText: TextView
    lateinit var listview: ListView
    lateinit var backBtn: Button
    lateinit var adapter: ArrayAdapter<String>
    private val dataList = mutableListOf<String>()
    private lateinit var provinceList: MutableList<Province>
    private lateinit var cityList: MutableList<City>
    private lateinit var countyList: MutableList<County>
    lateinit var selectedProvince: Province
    lateinit var selectedCity: City
    lateinit var selectedCounty: County
    var currentLevel = 0

    companion object {
        const val LEVEL_PROVINCE = 0
        const val LEVEL_CITY = 1
        const val LEVEL_COUNTY = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.choose_area, container, false)
        backBtn = view.findViewById(R.id.back_button)
        listview = view.findViewById(R.id.list_view)
        titleText = view.findViewById(R.id.title_text)
        adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, dataList)
        listview.adapter = adapter
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listview.setOnItemClickListener { parent, view, position, id ->
            when (currentLevel) {
                LEVEL_PROVINCE -> {
                    selectedProvince = provinceList[position]
                    queryCities()
                }
                LEVEL_CITY -> {
                    selectedCity = cityList[position]
                    queryCounties()
                }
                LEVEL_COUNTY -> {
                    selectedCounty = countyList[position]
                    when (activity) {
                        is MainActivity -> {
                            selectedCounty.weatherId?.let {
                                val intent = Intent(activity, WeatherActivity::class.java)
                                intent.putExtra("weather_id", it)
                                startActivity(intent)
                                activity?.finish()
                            }
                        }
                        is WeatherActivity -> {
                            (activity as WeatherActivity).drawerLayout.closeDrawers()
                            (activity as WeatherActivity).swipeFreshLayout.isRefreshing = true
                            (activity as WeatherActivity).requestWeatherInfo(selectedCounty.weatherId)

                        }
                    }

                }
            }
        }
        backBtn.setOnClickListener {
            when (currentLevel) {
                LEVEL_CITY -> {
                    queryProvinces()
                }
                LEVEL_COUNTY -> {
                    queryCities()
                }
            }
        }
        queryProvinces()
    }

    private fun queryCounties() {
        titleText.text = selectedCity.cityName
        backBtn.visibility = View.VISIBLE
        countyList =
            LitePal.where("cityid = ?", selectedCity.id.toString()).find(County::class.java)
        if (countyList.isNotEmpty()) {
            dataList.clear()
            for (county in countyList) {
                dataList.add(county.countyName!!)
            }
            adapter.notifyDataSetChanged()
            listview.setSelection(0)
            currentLevel = LEVEL_COUNTY
        } else {
            val url =
                "http://guolin.tech/api/china/${selectedProvince.provinceCode}/${selectedCity.cityCode}"
            queryFromServer(url, LEVEL_COUNTY)
        }
    }

    private fun queryCities() {
        titleText.text = selectedProvince.provinceName
        backBtn.visibility = View.VISIBLE
        cityList =
            LitePal.where("provinceid = ?", selectedProvince.id.toString()).find(City::class.java)
        if (cityList.isNotEmpty()) {
            dataList.clear()
            for (city in cityList) {
                dataList.add(city.cityName!!)
            }
            adapter.notifyDataSetChanged()
            listview.setSelection(0)
            currentLevel = LEVEL_CITY
        } else {
            val url = "http://guolin.tech/api/china/${selectedProvince.provinceCode}"
            queryFromServer(url, LEVEL_CITY)
        }
    }

    private fun queryProvinces() {
        titleText.text = "中国"
        backBtn.visibility = View.GONE
        provinceList = LitePal.findAll(Province::class.java)
        if (provinceList.isNotEmpty()) {
            dataList.clear()
            for (province in provinceList) {
                dataList.add(province.provinceName!!)
            }
            adapter.notifyDataSetChanged()
            listview.setSelection(0)
            currentLevel = LEVEL_PROVINCE
        } else {
            val url = "http://guolin.tech/api/china"
            queryFromServer(url, LEVEL_PROVINCE)
        }
    }

    val TAG = "ChooseAreaFragment"
    private fun queryFromServer(url: String, type: Int) {
        sendHttpRequest(url, object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "onFailure: ${e.message}")
                Log.e(TAG, "onFailure: ${e.cause}")
                activity?.runOnUiThread {
                    closeProgressDialog()
                    Toast.makeText(activity, "加载失败", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val respText = response.body!!.string()
                Log.d(TAG, "onResponse: $respText")
                var res = false
                when (type) {
                    LEVEL_CITY -> {
                        res = handleCityResponse(respText, selectedProvince.id!!)
                    }
                    LEVEL_COUNTY -> {
                        res = handleCountyResponse(respText, selectedCity.id!!)
                    }
                    LEVEL_PROVINCE -> {
                        res = handleProvinceResponse(respText)
                    }
                }
                if (res) {
                    activity?.runOnUiThread {
                        closeProgressDialog()
                        when (type) {
                            LEVEL_CITY -> {
                                queryCities()
                            }
                            LEVEL_COUNTY -> {
                                queryCounties()
                            }
                            LEVEL_PROVINCE -> {
                                queryProvinces()
                            }
                        }
                    }
                }
            }
        })
    }

    fun showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(activity)
            progressDialog?.setMessage("加载中。。。")
            progressDialog?.setCanceledOnTouchOutside(false)
        }
        progressDialog?.show()
    }

    fun closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog?.dismiss()
        }
    }
}
