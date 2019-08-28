package com.yujin.weathercam.Data

enum class WeatherInfo(val weather_kr: String, val weather_res: Int, val RGBA: String) {
    Thunderstorm("천둥번개", -1, "#0ae1ce00"),
    Drizzle("이슬비", -1, "#0b3262ff"),
    Rain("비", -1, "#0e486c83"),
    Snow("눈", -1, "#0ed8d7d5"),
    Mist("옅은 안개", -1, "#10d7dfff"),
    Haze("실안개", -1, "#14d7dfff"),
    Dust("황사", -1, "#16d1c1ba"),
    Fog("안개", -1, "#18d7dfff"),
    Sand("모래", -1, "#1ff2d1a9"),
    Ash("잿가루", -1, "#1ff2f3e9"),
    Squall("강한 바람", -1, "#0e3779f6"),
    Tornado("토네이도", -1, "#1ba2aabe"),
    Clear("맑음", -1, "#0ed1f7ff"),
    Clouds("흐림", -1, "#13d1ced9")
}