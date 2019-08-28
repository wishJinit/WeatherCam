package com.yujin.weathercam

import androidx.databinding.ObservableField


class WeatherVO{
    var weather = ObservableField<String>()
    var weather_kr = ObservableField<String>()
    var description = ObservableField<String>()
    var filterColor = ObservableField<String>()
}