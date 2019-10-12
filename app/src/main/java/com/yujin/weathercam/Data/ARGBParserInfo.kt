package com.yujin.weathercam.Data

enum class ARGBParserInfo(val start:Int, val end: Int) {
    ALPHA(1,3),
    RED(3,5),
    GREEN(5,7),
    BLUE(7,9)
}