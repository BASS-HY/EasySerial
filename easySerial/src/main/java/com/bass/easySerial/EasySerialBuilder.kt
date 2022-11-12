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

@file:Suppress("UNUSED")

package com.bass.easySerial

import com.bass.easySerial.enums.*
import com.bass.easySerial.extend.logE
import com.bass.easySerial.wrapper.BaseEasySerialPort
import com.bass.easySerial.wrapper.EasyWaitRspPort
import com.bass.easySerial.wrapper.EasyKeepReceivePort
import java.io.File
import java.io.IOException
import java.security.InvalidParameterException
import java.util.concurrent.ConcurrentHashMap

/**
 * Create by BASS
 * on 2021/12/24 9:00.
 * 创建串口以及设置全局配置
 */
object EasySerialBuilder {

    //保存所有生成的串口
    private val serialPortMap by lazy { ConcurrentHashMap<String, BaseEasySerialPort>() }

    //不可读取字节数的串口名称
    internal val noAvailableList by lazy { mutableListOf<String>() }

    //是否显示日志
    internal var showLog = true

    /**
     * 创建等待结果返回的串口对象
     * 写入后将阻塞写入的协程,并等待结果返回
     */
    fun createWaitRspPort(path: String, baudRate: BaudRate, flags: Int = 0) =
        initWaitResponse(path, baudRate, flags)

    /**
     * 创建等待结果返回的串口对象
     */
    fun createWaitRspPort(
        path: String,
        baudRate: BaudRate,
        dataBits: DataBit,
        stopBits: StopBit,
        parity: Parity,
        flags: Int = 0,
        flowCon: FlowCon = FlowCon.NONE
    ) = initWaitResponse(path, baudRate, flags, dataBits, stopBits, parity, flowCon)

    //开始创建串口对象
    private fun initWaitResponse(
        path: String,
        baudRate: BaudRate,
        flags: Int,
        dataBits: DataBit = DataBit.CSEmpty,
        stopBits: StopBit = StopBit.BEmpty,
        parity: Parity = Parity.NONE,
        flowCon: FlowCon = FlowCon.NONE
    ): EasyWaitRspPort? {
        try {
            serialPortMap.forEach {
                if (it.key == path) return it.value as EasyWaitRspPort
            }
            if (path.isEmpty() || baudRate.baudRate == -1) throw InvalidParameterException()

            val serialPort = SerialPort(
                File(path),
                baudRate.baudRate,
                flags,
                dataBits.dataBit,
                stopBits.stopBit,
                parity.parity,
                flowCon.flowCon
            )
            val serialPortChat = EasyWaitRspPort(serialPort)
            serialPortMap[path] = serialPortChat
            return serialPortChat
        } catch (e: SecurityException) {
            logE("You do not have read/write permission to the serialPort.")
            return null
        } catch (e: IOException) {
            logE("The serial port can not be opened for an unknown reason.")
            return null
        } catch (e: InvalidParameterException) {
            logE("Please configure your serial port first.")
            return null
        }
    }

    //------------------------ 创建一个可以永远接收的串口 -------------------------------
    /**
     * 创建一个永远保持接收的串口对象
     * 写入时不会发送阻塞
     */
    fun <CallBackType> createKeepReceivePort(path: String, baudRate: BaudRate, flags: Int = 0) =
        initReceive<CallBackType>(path, baudRate, flags)

    /**
     * 创建一个永远保持接收的串口对象
     * 写入时不会发送阻塞
     */
    fun <CallBackType> createKeepReceivePort(
        path: String,
        baudRate: BaudRate,
        dataBits: DataBit,
        stopBits: StopBit,
        parity: Parity,
        flags: Int = 0,
        flowCon: FlowCon = FlowCon.NONE
    ) = initReceive<CallBackType>(path, baudRate, flags, dataBits, stopBits, parity, flowCon)

    //开始创建串口对象
    @Suppress("UNCHECKED_CAST")
    private fun <CallBackType> initReceive(
        path: String,
        baudRate: BaudRate,
        flags: Int,
        dataBits: DataBit = DataBit.CSEmpty,
        stopBits: StopBit = StopBit.BEmpty,
        parity: Parity = Parity.NONE,
        flowCon: FlowCon = FlowCon.NONE
    ): EasyKeepReceivePort<CallBackType>? {
        try {
            serialPortMap.forEach {
                if (it.key == path) return it.value as EasyKeepReceivePort<CallBackType>
            }
            if (path.isEmpty() || baudRate.baudRate == -1) throw InvalidParameterException()

            val serialPort = SerialPort(
                File(path),
                baudRate.baudRate,
                flags,
                dataBits.dataBit,
                stopBits.stopBit,
                parity.parity,
                flowCon.flowCon
            )
            val serialPortChat = EasyKeepReceivePort<CallBackType>(serialPort)
            serialPortMap[path] = serialPortChat
            return serialPortChat
        } catch (e: SecurityException) {
            logE("You do not have read/write permission to the serialPort.")
            return null
        } catch (e: IOException) {
            logE("The serial port can not be opened for an unknown reason.")
            return null
        } catch (e: InvalidParameterException) {
            logE("Please configure your serial port first.")
            return null
        }
    }

    /**
     * 获取指定接口已经存在的实例
     * @param path 串口名称
     * @return 返回串口对象 如果没有生成 那么将返回 Null
     */
    fun get(path: String): BaseEasySerialPort? {
        serialPortMap.forEach {
            if (it.key == path) return it.value
        }
        return null
    }

    /**
     * 判断是否有串口正在工作
     */
    fun hasPortWorking() = serialPortMap.size > 0

    /**
     * 添加不可读取字节数的串口名称
     * 部分串口会有无法读取字节数的问题,如果你确定串口配置是无误的却没有接收到数据,
     * 那么你可以尝试调用此方法后再创建串口
     * 一定要先调用此方法,再调用创建串口的方法,此方法才能生效
     * @param devicePath 例如:/dev/ttyS4
     */
    fun addNoAvailableDevicePath(devicePath: String): EasySerialBuilder {
        noAvailableList.find { it == devicePath } ?: noAvailableList.add(devicePath)
        return this
    }

    /**
     * 移除不可读取字节数的串口名称
     * @param devicePath 例如:/dev/ttyS4
     */
    fun removeNoAvailableDevicePath(devicePath: String): EasySerialBuilder {
        noAvailableList.remove(devicePath)
        return this
    }

    /**
     * 设置是否打印日志
     * @param show true为打印日志,否则反之,默认为打印日志
     */
    fun isShowLog(show: Boolean): EasySerialBuilder {
        showLog = show
        return this
    }

    //串口关闭时移除实例,只被内部调用,不对外开放
    internal fun remove(serialPortChat: BaseEasySerialPort) {
        serialPortMap.forEach {
            if (it.value === serialPortChat) {
                serialPortMap.remove(it.key)
                return@forEach
            }
        }
    }
}