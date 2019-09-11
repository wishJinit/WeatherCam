package com.yujin.weathercam

import androidx.databinding.ObservableField


class WeatherVO {
    var weather = ObservableField<String>()
    var weather_kr = ObservableField<String>("WeatherCam")
    var description = ObservableField<String>()
    var filterColor = ObservableField<String>("#00000000")
}