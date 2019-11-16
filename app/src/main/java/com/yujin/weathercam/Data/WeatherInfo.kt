package com.yujin.weathercam.Data

enum class WeatherInfo(val weather_kr: String, val weather_res: Int, val RGBA: String) {
    Thunderstorm("천둥번개", -1, "#14f0d16e"),
    Drizzle("이슬비", -1, "#197287b0"),
    Rain("비", -1, "#237287b0"),
    Snow("눈", -1, "#1be6e6e6"),
    Mist("옅은 안개", -1, "#10d7dfff"),
    Haze("실안개", -1, "#14d7dfff"),
    Dust("황사", -1, "#218f784d"),
    Fog("안개", -1, "#18d7dfff"),
    Sand("모래", -1, "#1ff2d1a9"),
    Ash("잿가루", -1, "#1ff2f3e9"),
    Squall("강한 바람", -1, "#0e3779f6"),
    Tornado("토네이도", -1, "#1ba2aabe"),
    Clear("맑음", -1, "#17b5e4ff"),
    Clouds("흐림", -1, "#23d1ced9")
}