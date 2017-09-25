/*
 * Copyright (c) 2012-2013 NetEase, Inc. and other contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.example.henry.optimizer2.utils

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.InputStreamReader

import android.app.ActivityManager
import android.content.Context
import android.util.Log

/**
 * operate memory information
 *
 * @author andrewleo
 */
class MemoryInfo {

    /**
     * get total memory of certain device.
     *
     * @return total memory of device
     */
    val getTotalMemory: Long
        get() {
            val memInfoPath = "/proc/meminfo"
            var readTemp = ""
            var memTotal = ""
            var memory: Long = 0
            try {
                val fr = FileReader(memInfoPath)
                val localBufferedReader = BufferedReader(fr, 8192)
                readTemp = localBufferedReader.readLine()
                while ((readTemp) != null) {
                    if (readTemp.contains("MemTotal")) {
                        val total = readTemp.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        memTotal = total[1].trim { it <= ' ' }
                    }
                }
                localBufferedReader.close()
                val memKb = memTotal.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                memTotal = memKb[0].trim { it <= ' ' }
                //Log.d(LOG_TAG, "memTotal: " + memTotal)
                memory = java.lang.Long.parseLong(memTotal)
            } catch (e: IOException) {
                //Log.e(LOG_TAG, "IOException: " + e.message)
            }

            return memory
        }

    /**
     * get free memory.
     *
     * @return free memory of device
     */
    fun getFreeMemorySize(context: Context): Long {
        val outInfo = ActivityManager.MemoryInfo()
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        am.getMemoryInfo(outInfo)
        val avaliMem = outInfo.availMem
        return avaliMem / 1024
    }

    /**
     * get the memory of process with certain pid.
     *
     * @param pid
     * pid of process
     * @param context
     * context of certain activity
     * @return memory usage of certain process
     */
    fun getPidMemorySize(pid: Int, context: Context): Int {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val myMempid = intArrayOf(pid)
        val memoryInfo = am.getProcessMemoryInfo(myMempid)
        memoryInfo[0].totalSharedDirty
        return memoryInfo[0].totalPss
    }

    /**
     * get the sdk version of phone.
     *
     * @return sdk version
     */
    val sdkVersion: String
        get() = android.os.Build.VERSION.RELEASE

    /**
     * get phone type.
     *
     * @return phone type
     */
    val phoneType: String
        get() = android.os.Build.MODEL

}
