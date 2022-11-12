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

package com.bass.easySerial.wrapper

import com.bass.easySerial.EasySerialBuilder
import com.bass.easySerial.SerialPort

/**
 * Create by BASS
 * on 2021/12/23 17:47.
 * 串口通信的基类
 */
@Suppress("unused", "SpellCheckingInspection")
abstract class BaseEasySerialPort internal constructor(protected val serialPort: SerialPort) {

    protected var customMaxReadSize = 64//串口每次从数据流中读取的最大字节数

    /**
     * 获取串口的名称
     * 如：/dev/ttyS4
     */
    fun getPortPath() = serialPort.getDevicePath()

    /**
     * 强转成 [EasyKeepReceivePort]
     * @exception ClassCastException 如果类型不匹配,则抛出异常
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(ClassCastException::class)
    fun <CallBackType> cast2KeepReceivePort(): EasyKeepReceivePort<CallBackType> {
        return this as EasyKeepReceivePort<CallBackType>
    }

    /**
     * 强转成[EasyWaitRspPort]
     * @exception ClassCastException 如果类型不匹配,则抛出异常
     */
    @Throws(ClassCastException::class)
    fun cast2WaitRspPort(): EasyWaitRspPort {
        return this as EasyWaitRspPort
    }

    /**
     * 调用此方法将关闭串口
     */
    open suspend fun close() {
        //关闭串口
        serialPort.closeSerial()
        //移除串口类实例,下次才可再创建
        EasySerialBuilder.remove(this)
    }

}