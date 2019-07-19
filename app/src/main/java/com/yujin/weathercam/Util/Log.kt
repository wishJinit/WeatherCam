package com.yujin.weathercam.Util

import kotlin.text.StringBuilder

class Log {

    companion object {
        private val REAL_METHOD_POS:Int = 2

        fun d (tag : String, msg : String){
            android.util.Log.d(tag, (getPrefix() + msg))
        }

        fun e(tag : String, msg : String){
            android.util.Log.e(tag, (getPrefix() + msg))
        }

        /**
         * 로그가 들어 있는 함수명과 라인 출력
         *
         * @return (String) 함수명, 로그출력라인, 메서드 이름
         */
        private fun getPrefix(): String{
            val sb : StringBuilder = StringBuilder(1024)
            try{
                var ste:Array<StackTraceElement> = Throwable().stackTrace
                val realMethod:StackTraceElement = ste[REAL_METHOD_POS]

                sb.append("[")
                sb.append(realMethod.fileName)
                sb.append(":")
                sb.append(realMethod.lineNumber)
                sb.append(":")
                sb.append(realMethod.methodName)
                sb.append("()]")
            }catch (e:Exception){
                e.stackTrace
            }
            return sb.toString()
        }
    }

}
