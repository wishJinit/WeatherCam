package com.yujin.weathercam.Data

enum class WeatherInfo (val weather_kr: String, val weather_res: Int) {
    Thunderstorm("천둥번개", -1),
    Drizzle("이슬비", -1),
    Rain("비", -1),
    Snow("눈", -1),
    Mist("옅은 안개",-1),
    Haze("실안개", -1),
    Dust("황사", -1),
    Fog("안개",-1),
    Sand("모래", -1),
    Ash("잿가루", -1),
    Squall("강한 바람", -1),
    Tornado("토네이도", -1),
    Clear("맑음", -1),
    Clouds("흐림", -1),

}