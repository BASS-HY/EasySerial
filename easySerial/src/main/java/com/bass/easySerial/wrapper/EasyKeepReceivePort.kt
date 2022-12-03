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

import com.bass.easySerial.SerialPort
import com.bass.easySerial.handle.EasyPortDataHandle
import com.bass.easySerial.interfaces.EasyReadDataCallBack
import com.bass.easySerial.interfaces.EasyReceiveCallBack
import com.bass.easySerial.extend.logPortReceiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Create by BASS
 * on 2021/12/24 9:00.
 *
 * 通过 **[com.bass.easySerial.EasySerialBuilder.createKeepReceivePort]** 生成本类对象
 *
 * 接收数据:永远保持接收状态(耗费cpu); 适合:串口主动传输到客户端
 * 发送数据:不阻塞，不需要等待结果返回
 *
 * @param CallBackType 返回的数据类型
 */
@Suppress("UNUSED")
class EasyKeepReceivePort<CallBackType> internal constructor(serialPort: SerialPort) :
    BaseEasySerialPort(serialPort), EasyReadDataCallBack {

    private val callBackList by lazy { CopyOnWriteArrayList<EasyReceiveCallBack<CallBackType>>() }//监听数据返回
    private var dataHandle: EasyPortDataHandle<CallBackType>? = null//数据处理类
    private val openReceiveMutex by lazy { Mutex() }//开启串口接收的同步锁,防止多次开启
    private var isStart = false//标志是否已经开启了数据监听

    @Suppress("UNCHECKED_CAST")
    //监听串口数据
    override suspend fun receiveData(bytes: ByteArray) {
        CoroutineScope(Dispatchers.IO).launch {//开启顶层协程,不阻塞串口的读取
            logPortReceiveData(bytes)//输出获取到的数据
            dataHandle?.apply {//自定义了数据处理,则处理数据
                val dataList = receivePortData(bytes)
                callBackList.forEach { it.receiveData(dataList) }//处理完成数据后发生给监听者
            } ?: run {//没有自定义数据处理,则直接返回原始数据
                try {
                    callBackList.forEach { it.receiveData(listOf(bytes as CallBackType)) }
                } catch (e: Exception) {
                    throw RuntimeException(
                        "如果您没有设置数据处理方法,则传入的CallBackType类型应当为ByteArray,否则将无法进行数据转换",
                        e.cause
                    )
                }
            }
        }
    }

    /**
     * 添加串口数据监听,可在不同地方添加多个监听
     * @param callBack 监听
     */
    fun addDataCallBack(callBack: EasyReceiveCallBack<CallBackType>) {
        callBackList.add(callBack)
        start()//开启串口接收
    }

    /**
     * 添加串口数据监听,可在不同地方添加多个监听
     * @param callBack 监听
     */
    fun addDataCallBack(callBack: suspend (List<CallBackType>) -> Unit): EasyReceiveCallBack<CallBackType> {
        val receiveCallBack = object : EasyReceiveCallBack<CallBackType> {
            override suspend fun receiveData(dataList: List<CallBackType>) {
                callBack(dataList)
            }
        }
        callBackList.add(receiveCallBack)
        start()//开启串口接收
        return receiveCallBack
    }

    /**
     * 移除指定的串口监听
     */
    fun removeDataCallBack(callBack: EasyReceiveCallBack<CallBackType>) {
        callBackList.remove(callBack)
    }

    /**
     * 设置数据处理方法，接收到串口数据后会对数据进行处理后返回
     * @param dataHandle 自定义的数据处理类
     */
    fun setDataHandle(dataHandle: EasyPortDataHandle<CallBackType>): EasyKeepReceivePort<CallBackType> {
        this.dataHandle = dataHandle
        return this
    }

    /**
     * 设置串口每次从数据流中读取的最大字节数；
     * 必须在调用[addDataCallBack]之前设置，否则设置无效；
     * @param max 指定串口每次从数据流中读取的最大字节数；
     */
    fun setMaxReadSize(max: Int): EasyKeepReceivePort<CallBackType> {
        customMaxReadSize = max
        return this
    }

    /**
     * 设置串口数据读取的间隔 单位为毫秒；
     * 默认为10毫秒，读取时间越短，CPU的占用会越高，请合理配置此设置；
     * @param interval 间隔时间(毫秒)
     */
    fun setReadInterval(interval: Long): EasyKeepReceivePort<CallBackType> {
        serialPort.setReadInterval(interval)
        return this
    }

    /**
     * 写入一个数据
     * @param byteArray 写入的数据
     */
    fun write(byteArray: ByteArray) {
        serialPort.write(byteArray)
    }

    //开始接收数据
    private fun start() {
        if (openReceiveMutex.isLocked || isStart) return
        CoroutineScope(Dispatchers.IO).launch {
            openReceiveMutex.withLock {
                if (isStart) return@launch//已经开启了则不再开启
                serialPort.setReadDataCallBack(this@EasyKeepReceivePort)
                serialPort.startRead(customMaxReadSize)
                isStart = true
            }
        }
    }

    override suspend fun close() {
        callBackList.clear()
        dataHandle?.close()
        dataHandle = null
        super.close()
    }

}
