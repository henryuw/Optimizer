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

import java.io.IOException
import java.io.RandomAccessFile

import android.net.TrafficStats
import android.util.Log

/**
 * information of network traffic
 *
 * @author andrewleo
 */
class TrafficInfo(private val uid: String) {

    private val LOG_TAG = "Emmagee-" + TrafficInfo::class.java.simpleName
    private val UNSUPPORTED = -1

    /**
     * get total network traffic, which is the sum of upload and download
     * traffic.
     *
     * @return total traffic include received and send traffic
     */
    val trafficInfo: Long
        get() {
            Log.i(LOG_TAG, "get traffic information")
            Log.d(LOG_TAG, "uid = " + uid)
            val traffic = trafficFromApi()
            return if (traffic <= 0) trafficFromFiles() else traffic
        }

    /**
     * Use TrafficStats getUidRxBytes and getUidTxBytes to get network
     * traffic,these API return both tcp and udp usage
     *
     * @return
     */
    private fun trafficFromApi(): Long {
        var rcvTraffic = UNSUPPORTED.toLong()
        var sndTraffic = UNSUPPORTED.toLong()
        rcvTraffic = TrafficStats.getUidRxBytes(Integer.parseInt(uid))
        sndTraffic = TrafficStats.getUidTxBytes(Integer.parseInt(uid))
        return if (rcvTraffic + sndTraffic < 0) UNSUPPORTED as Long else rcvTraffic + sndTraffic
    }

    /**
     * read files in uid_stat to get traffic info
     *
     * @return
     */
    private fun trafficFromFiles(): Long {
        var rafRcv: RandomAccessFile? = null
        var rafSnd: RandomAccessFile? = null
        var rcvTraffic = UNSUPPORTED.toLong()
        var sndTraffic = UNSUPPORTED.toLong()
        val rcvPath = "/proc/uid_stat/$uid/tcp_rcv"
        val sndPath = "/proc/uid_stat/$uid/tcp_snd"
        try {
            rafRcv = RandomAccessFile(rcvPath, "r")
            rafSnd = RandomAccessFile(sndPath, "r")
            rcvTraffic = java.lang.Long.parseLong(rafRcv.readLine())
            sndTraffic = java.lang.Long.parseLong(rafSnd.readLine())
            Log.d(LOG_TAG, String.format("rcvTraffic, sndTraffic = %s, %s", rcvTraffic, sndTraffic))
        } catch (e: Exception) {
        } finally {
            try {
                if (rafRcv != null) {
                    rafRcv.close()
                }
                if (rafSnd != null)
                    rafSnd.close()
            } catch (e: IOException) {
            }

        }
        return if (rcvTraffic + sndTraffic < 0) UNSUPPORTED as Long else rcvTraffic + sndTraffic
    }

    companion object {

        private val LOG_TAG = "Emmagee-" + TrafficInfo::class.java.simpleName
        private val UNSUPPORTED = -1
    }

}
