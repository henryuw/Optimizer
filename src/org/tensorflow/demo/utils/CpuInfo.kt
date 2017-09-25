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

import java.io.File
import java.io.FileFilter
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.regex.Pattern


import android.content.Context
import android.util.Log

/**
 * operate CPU information
 *
 * @author andrewleo
 */
class CpuInfo(private val context: Context, private val pid: Int, uid: String) {
    private var processCpu: Long = 0
    private val idleCpu = ArrayList<Long>()
    private val totalCpu = ArrayList<Long>()
    private var isInitialStatics = true
    private val formatterFile: SimpleDateFormat
    private val mi: MemoryInfo
    private val totalMemorySize: Long
    private var preTraffic: Long = 0
    private var lastestTraffic: Long = 0
    private var traffic: Long = 0
    private val trafficInfo: TrafficInfo
    private var cpuUsedRatio = ArrayList<String>()
    private var totalCpu2: ArrayList<Long>? = ArrayList()
    private var processCpu2: Long = 0
    private var idleCpu2 = ArrayList<Long>()
    private var processCpuRatio = ""
    private val totalCpuRatio = ArrayList<String>()

    init {
        trafficInfo = TrafficInfo(uid)
        formatterFile = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        mi = MemoryInfo()
        totalMemorySize = mi.getTotalMemory
        cpuUsedRatio = ArrayList()
    }

    /**
     * read the status of CPU.
     *
     * @throws FileNotFoundException
     */
    fun readCpuStat() {
        val processPid = Integer.toString(pid)
        val cpuStatPath = "/proc/$processPid/stat"
        try {
            // monitor cpu stat of certain process
            val processCpuInfo = RandomAccessFile(cpuStatPath, "r")
            var line = ""
            val stringBuffer = StringBuffer()
            stringBuffer.setLength(0)
            line = processCpuInfo.readLine()
            while (line != null) {
                stringBuffer.append(line + "\n")
            }
            val tok = stringBuffer.toString().split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            processCpu = java.lang.Long.parseLong(tok[13]) + java.lang.Long.parseLong(tok[14])
            processCpuInfo.close()
        } catch (e: FileNotFoundException) {
            Log.w(LOG_TAG, "FileNotFoundException: " + e.message)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        readTotalCpuStat()
    }

    /**
     * read stat of each CPU cores
     */
    private fun readTotalCpuStat() {
        try {
            // monitor total and idle cpu stat of certain process
            val cpuInfo = RandomAccessFile(CPU_STAT, "r")
            var line = ""
            line = cpuInfo.readLine()
            while (null != (line)  && line.startsWith("cpu")) {
                val toks = line.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                idleCpu.add(java.lang.Long.parseLong(toks[4]))
                totalCpu.add(java.lang.Long.parseLong(toks[1]) + java.lang.Long.parseLong(toks[2]) + java.lang.Long.parseLong(toks[3]) + java.lang.Long.parseLong(toks[4])
                        + java.lang.Long.parseLong(toks[6]) + java.lang.Long.parseLong(toks[5]) + java.lang.Long.parseLong(toks[7]))
            }
            cpuInfo.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    /**
     * get CPU name.
     *
     * @return CPU name
     */
    // check cpu type
    val cpuName: String
        get() {
            try {
                val cpuStat = RandomAccessFile(CPU_INFO_PATH, "r")
                var line: String
                line = cpuStat.readLine()
                while (null != (line)) {
                    val values = line.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (values[0].contains(INTEL_CPU_NAME) || values[0].contains("Processor")) {
                        cpuStat.close()
                        Log.d(LOG_TAG, "CPU name=" + values[1])
                        return values[1]
                    }
                }
            } catch (e: IOException) {
                Log.e(LOG_TAG, "IOException: " + e.message)
            }

            return ""
        }

    /**
     * display directories naming with "cpu*"
     *
     * @author andrewleo
     */
    internal inner class CpuFilter : FileFilter {
        override fun accept(pathname: File): Boolean {
            // Check if filename matchs "cpu[0-9]"
            return if (Pattern.matches("cpu[0-9]", pathname.name)) {
                true
            } else false
        }
    }

    /**
     * get CPU core numbers
     *
     * @return cpu core numbers
     */
    // Get directory containing CPU info
    // Filter to only list the devices we care about
    val cpuNum: Int
        get() {
            try {
                val dir = File(CPU_DIR_PATH)
                val files = dir.listFiles(CpuFilter())
                return files!!.size
            } catch (e: Exception) {
                e.printStackTrace()
                return 1
            }

        }

    /**
     * get CPU core list
     *
     * @return cpu core list
     */
    // Get directory containing CPU info
    // Filter to only list the devices we care about
    val cpuList: ArrayList<String>
        get() {
            val cpuList = ArrayList<String>()
            try {
                val dir = File(CPU_DIR_PATH)
                val files = dir.listFiles(CpuFilter())
                for (i in files!!.indices) {
                    cpuList.add(files[i].name)
                }
                return cpuList
            } catch (e: Exception) {
                e.printStackTrace()
                cpuList.add("cpu0")
                return cpuList
            }

        }


    /**
     * is text a positive number
     *
     * @param text
     * @return
     */
    private fun isPositive(text: String): Boolean {
        val num: Double?
        try {
            num = java.lang.Double.parseDouble(text)
        } catch (e: NumberFormatException) {
            return false
        }

        return num >= 0
    }

    companion object {

        private val LOG_TAG = "Emmagee-" + CpuInfo::class.java.simpleName

        private val INTEL_CPU_NAME = "model name"
        private val CPU_DIR_PATH = "/sys/devices/system/cpu/"
        private val CPU_X86 = "x86"
        private val CPU_INFO_PATH = "/proc/cpuinfo"
        private val CPU_STAT = "/proc/stat"
    }

}
