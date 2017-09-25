package com.example.henry.optimizer2.utils

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader

class FPSInfo {

    private var process: Process? = null
    private var ir: BufferedReader? = null
    private var os: DataOutputStream? = null
    private var startTime = 0L
    private var lastFrameNum = 0
    private var ok = true

    /**
     * get frame per second
     *
     * @return frame per second
     */
    fun fps(): Float {
        if (ok) {
            val nowTime = System.nanoTime()
            val f = (nowTime - startTime).toFloat() / 1000000.0f
            startTime = nowTime
            val nowFrameNum = frameNum
            val fps = Math.round((nowFrameNum - lastFrameNum) * 1000 / f).toFloat()
            lastFrameNum = nowFrameNum
            return fps
        } else {
            return -1f
        }

    }

    /**
     * get frame value
     *
     * @return frame value
     */
    val frameNum: Int
        get() {
            try {
                if (process == null) {
                    process = Runtime.getRuntime().exec("su")
                    os = DataOutputStream(process!!.outputStream)
                    ir = BufferedReader(InputStreamReader(
                            process!!.inputStream))
                }
                os!!.writeBytes("service call SurfaceFlinger 1013" + "\n")
                os!!.flush()
                val str1 = ir!!.readLine()
                if (str1 != null) {
                    val start = str1.indexOf("(")
                    val end = str1.indexOf("  ")
                    if ((start != -1) and (end > start)) {
                        val str2 = str1.substring(start + 1, end)
                        return Integer.parseInt(str2, 16)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            ok = false
            return -1
        }
}
