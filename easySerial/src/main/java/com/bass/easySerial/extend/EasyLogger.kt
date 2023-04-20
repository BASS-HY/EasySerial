/*
 *   Copyright 2022 BASS
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.bass.easySerial.extend

import android.util.Log
import com.bass.easySerial.EasySerialBuilder
import java.nio.charset.Charset

internal const val TAG = "EasyPort"
internal val encoder by lazy { Charset.forName("gbk").newEncoder() }

@Suppress("UNUSED")
internal fun logD(d: String) {
    if (EasySerialBuilder.showLog) Log.d(TAG, d)
}

@Suppress("UNUSED")
internal fun logE(e: String) {
    if (EasySerialBuilder.showLog) Log.e(TAG, e)
}

internal fun logE(e: Exception) {
    if (EasySerialBuilder.showLog) Log.e(TAG, e.stackTraceToString())
}

internal fun logE(e: Throwable) {
    if (EasySerialBuilder.showLog) Log.e(TAG, e.stackTraceToString())
}

@Suppress("UNUSED")
internal fun logI(i: String) {
    if (EasySerialBuilder.showLog) Log.i(TAG, i)
}

internal fun logPortSendData(bytes: ByteArray?) {
    if (!EasySerialBuilder.showLog) return
    val decodeToString = bytes?.decodeToString()
    if (encoder.canEncode(decodeToString))
        logI("串口发起通信:${decodeToString}")
    else
        logI("串口发起通信:${bytes?.conver2HexStringWithBlank()}")
}

internal fun logPortReceiveData(bytes: ByteArray?) {
    if (!EasySerialBuilder.showLog) return
    try {
        val decodeToString = bytes?.decodeToString()
        if (tryCatch(false) { encoder.canEncode(decodeToString) }) logI("串口接收到数据:${decodeToString}")
        else logI("串口接收到数据:${bytes?.conver2HexStringWithBlank()}")
    } catch (e: Exception) {
        logI("串口接收到数据(无法转化为字符串或16进制形式):$bytes")
    }
}