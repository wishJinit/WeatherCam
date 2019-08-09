package com.yujin.weathercam.Net

import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitConnection {
    @GET("data/2.5/weather")
    fun weatherInfo(@Query("lat") lat:String,
                    @Query("lon") lon:String,
                    @Query("id") id:String)
            : Call<JSONObject>
}