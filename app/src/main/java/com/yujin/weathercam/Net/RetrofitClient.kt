package com.yujin.weathercam.Net

import com.google.gson.JsonObject
import com.yujin.weathercam.WeatherVO
import com.yujin.weathercam.Util.APIKey
import com.yujin.weathercam.Util.Log
import com.yujin.weathercam.Data.WeatherInfo
import com.yujin.weathercam.VO.LocationVO
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient {
    private val TAG = "RetrofitClient"
    private val BASE_URL = "http://api.openweathermap.org"

    private var retrofit: Retrofit
    private var service: RetrofitConnection

    init {
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(RetrofitConnection::class.java)
    }

    fun bringWeatherData(weatherInfo: WeatherVO, locationInfo: LocationVO) {
        var data = service.weatherInfo(locationInfo.lat, locationInfo.lon, APIKey.WEATHER_KEY)
        val obj = object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                t.message?.let { Log.d(TAG, it) }
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val weatherList = response.body()?.get("weather").toString()
                val weather = JSONObject(JSONArray(weatherList).get(0).toString())

                weather.let {
                    val weatherNm = it.get("main").toString()
                    weatherInfo.weather.set(weatherNm)
                    weatherInfo.weather_kr.set(WeatherInfo.valueOf(weatherNm).weather_kr)
                    weatherInfo.description.set(it.get("description").toString())
                    weatherInfo.filterColor.set(WeatherInfo.valueOf(weatherNm).RGBA)
                }
            }
        }

        data.enqueue(obj)
    }
}