package com.yujin.weathercam.Net

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.yujin.weathercam.Data.WeatherVO
import com.yujin.weathercam.Util.APIKey
import com.yujin.weathercam.Util.Log
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient{
    private val TAG = "RetrofitClient"
    private val BASE_URL = "http://api.openweathermap.org"

    lateinit var retrofit:Retrofit
    lateinit var service:RetrofitConnection

    fun bringWeatherData(){
        val weatherInfo:WeatherVO = WeatherVO()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(RetrofitConnection::class.java)
        var data = service.weatherInfo(55.5,57.5, APIKey.WEATHER_KEY)
        val obj = object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                t.message?.let { Log.d(TAG, it) }
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val weatherList = response.body()?.get("weather").toString()
                val weather = JSONObject(JSONArray(weatherList).get(0).toString())

                weatherInfo.weather = weather.get("main").toString()
                weatherInfo.description = weather.get("description").toString()
            }
        }

        data.enqueue(obj)
    }
}