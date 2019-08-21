package com.yujin.weathercam.Data

enum class WeatherInfo (val weather_kr: String, val weather_res: Int, val RGBA:String) {
    Thunderstorm("천둥번개", -1, "#00000000"),
    Drizzle("이슬비", -1, "#00000000"),
    Rain("비", -1, "#00000000"),
    Snow("눈", -1, "#00000000"),
    Mist("옅은 안개",-1, "#00000000"),
    Haze("실안개", -1, "#00000000"),
    Dust("황사", -1, "#00000000"),
    Fog("안개",-1, "#00000000"),
    Sand("모래", -1, "#00000000"),
    Ash("잿가루", -1, "#00000000"),
    Squall("강한 바람", -1, "#00000000"),
    Tornado("토네이도", -1, "#00000000"),
    Clear("맑음", -1, "#00000000"),
    Clouds("흐림", -1, "#00000000")
}