package com.yujin.weathercam.Util

import com.yujin.weathercam.Data.ARGBParserInfo
import java.lang.Exception

class ARGBParser(argb: String) {
    companion object {
        private val TAG = "ARGBParser"
    }

    private val DEFINE_FILTER = "#00000000"
    private val STR_LENGTH = DEFINE_FILTER.count()
    val RADIX_HEX = 16
    val HEX_MAX = "FF".toInt(RADIX_HEX).toFloat()

    var argb: String = DEFINE_FILTER
        set(value) {
            if (value.length == STR_LENGTH &&
                value.startsWith("#") &&
                isHex(value.substring(1))
            ) {
                field = value
            } else {
                field = DEFINE_FILTER
            }
        }


    init {
        this.argb = argb
    }

    fun getHex(color: ARGBParserInfo):Int = argb.substring(color.start, color.end).toInt(RADIX_HEX)

    fun getHexPercentageDecimal(color: ARGBParserInfo):Float = getHex(color)/HEX_MAX

    fun getHexPercentage(color: ARGBParserInfo):Int = (getHexPercentageDecimal(color)*100).toInt()

    private fun isHex(hex: String): Boolean {
        try {
            hex.toLong(RADIX_HEX).toString()
            return true
        } catch (e: Exception) {
            Log.d(TAG, "Filter Color is not fit the format.")
            return false
        }
    }
}
