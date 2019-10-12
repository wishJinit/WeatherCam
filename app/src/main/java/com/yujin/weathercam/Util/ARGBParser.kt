package com.yujin.weathercam.Util

import java.lang.Exception

class ARGBParser(argb: String) {
    companion object {
        private val TAG = "ARGBParser"
    }

    private val defineFilter = "#00000000"
    private val STR_LENGTH = defineFilter.count()

    var argb: String = defineFilter
        set(value) {
            if (value.length == STR_LENGTH &&
                value.startsWith("#") &&
                isHex(value.substring(1))
            ) {
                field = value
            } else {
                field = defineFilter
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
