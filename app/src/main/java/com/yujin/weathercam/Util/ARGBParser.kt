package com.yujin.weathercam.Util

import java.lang.Exception

class ARGBParser(argb: String) {
    companion object {
        private val TAG = "ARGBParser"
    }

    private val DEFINE_FILTER = "#00000000"
    private val STR_LENGTH = DEFINE_FILTER.count()

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

    private fun isHex(hex: String): Boolean {
        try {
            hex.toLong(16).toString()
            return true
        } catch (e: Exception) {
            Log.d(TAG, "Filter Color is not fit the format.")
            return false
        }
    }
}
