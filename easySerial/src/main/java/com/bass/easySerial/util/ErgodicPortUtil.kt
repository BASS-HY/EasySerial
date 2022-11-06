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

package com.bass.easySerial.util

import android.util.Log
import com.bass.easySerial.SerialPort
import com.bass.easySerial.bean.ErgodicBean
import com.bass.easySerial.enums.BaudRate
import com.bass.easySerial.enums.DataBit
import com.bass.easySerial.enums.Parity
import com.bass.easySerial.enums.StopBit
import com.bass.easySerial.extend.conver2ByteArray
import com.bass.easySerial.wrapper.EasyWaitRspPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * Create by BASS
 * on 2021/12/15 14:42.
 * 用于循环查找需要的串口,此方法处于实验阶段,暂未开放；
 */
@Suppress("UNUSED")
internal object ErgodicPortUtil {

    private var bytes2: ByteArray? = null
    private val successList by lazy { mutableListOf<ErgodicBean>() }
    private val baudRates by lazy { intArrayOf(BaudRate.B4800.baudRate, BaudRate.B9600.baudRate) }
    private val nBits by lazy { intArrayOf(
        DataBit.CS5.dataBit,
        DataBit.CS6.dataBit,
        DataBit.CS7.dataBit,
        DataBit.CS8.dataBit
    ) }
    private val nEvent by lazy { arrayOf(Parity.NONE, Parity.ODD, Parity.EVEN) }
    private val mStop by lazy { intArrayOf(StopBit.B1.stopBit, StopBit.B2.stopBit) }

    /**
     * @param hexStr 16进制串口查询指令
     */
    fun ergodic(hexStr: String) {
        bytes2 = hexStr.conver2ByteArray()
        CoroutineScope(Dispatchers.IO).launch {
            for (path in EasySerialFinderUtil.getAllDevicesPath()) {
                for (baudRate in baudRates) {
                    for (nBit in nBits) {
                        for (event in nEvent) {
                            for (stop in mStop) {
                                try {
                                    Log.i(
                                        "遍历串口",
                                        "本次尝试-->path: $path baudRate:$baudRate nBit:$nBit event:$event stop:$stop"
                                    )
                                    bytes2?.let {
                                        val serialPort = SerialPort(
                                            File(path),
                                            baudRate,
                                            0,
                                            nBit,
                                            stop,
                                            event.parity
                                        )
                                        val easyWaitRspPort = EasyWaitRspPort()
                                        easyWaitRspPort.initSerialPort(serialPort)
                                        val dataList = easyWaitRspPort.writeWaitRsp(it)
                                        if (dataList.size > 0) {
                                            Log.w(
                                                "遍历串口",
                                                "成功!!! path:$path baudRate:$baudRate nBit:$nBit event:$event stop:$stop \nreturned: ${it.contentToString()}"
                                            )
                                            ErgodicBean(
                                                path,
                                                baudRate,
                                                0,
                                                nBit,
                                                stop,
                                                event.parity
                                            ).apply {
                                                successList.add(this)
                                            }
                                        } else {
                                            Log.e(
                                                "遍历串口",
                                                "本次失败 path: $path baudRate:$baudRate nBit:$nBit event:$event stop:$stop"
                                            )
                                        }
                                        easyWaitRspPort.close()
                                    }
                                } catch (e: Exception) {
                                    Log.w(
                                        "遍历串口",
                                        "异常 path: $path baudRate:$baudRate nBit:$nBit event:$event stop:$stop"
                                    )
                                    Log.w("遍历串口", "原因:${e.cause}")
                                }
                            }
                        }
                    }
                }
            }
            Log.i("遍历串口", "成功如下:\n")
            successList.forEach {
                Log.i(
                    "遍历串口",
                    "path: ${it.path} baudRate:${it.baudRate} nBit:${it.dataBits} event:${it.parity} stop:${it.stopBits}"
                )
            }

        }
    }

}