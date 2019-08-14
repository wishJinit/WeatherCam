package com.yujin.weathercam.Net

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitConnection {
    @GET("data/2.5/weather")
    fun weatherInfo(@Query("lat") lat:Double,
                    @Query("lon") lon:Double,
                    @Query("APPID") id:String)
            : Call<JsonObject>

    @GET("data/2.5/weather")
    fun capitalCityInfo(@Query("q") capital:String,
                    @Query("APPID") id:String)
            : Call<JsonObject>
}