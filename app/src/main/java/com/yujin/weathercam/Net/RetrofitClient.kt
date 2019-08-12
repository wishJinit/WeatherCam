package com.yujin.weathercam.Net

import com.yujin.weathercam.Util.APIKey
import com.yujin.weathercam.Util.Log
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient{
    private val BASE_URL = "http://api.openweathermap.org"

    lateinit var retrofit:Retrofit
    lateinit var service:RetrofitConnection

    fun bringWeatherData(){
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(RetrofitConnection::class.java)
//        var data = service.weatherInfo(55.5,57.5, APIKey.WEATHER_KEY)
        var data = service.capitalCityInfo("London", APIKey.WEATHER_KEY)
        val obj = object : Callback<JSONObject> {
            override fun onFailure(call: Call<JSONObject>, t: Throwable) {
                print(t.message)
            }

            override fun onResponse(call: Call<JSONObject>, response: Response<JSONObject>) {
                print(response)
            }
        }

        data.enqueue(obj)
    }
}