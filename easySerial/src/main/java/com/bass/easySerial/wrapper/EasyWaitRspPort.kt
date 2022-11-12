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
import com.bass.easySerial.bean.WaitResponseBean
import com.bass.easySerial.interfaces.EasyReadDataCallBack
import com.bass.easySerial.extend.logPortReceiveData
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.ByteBuffer

/**
 * 通过 **[com.bass.easySerial.EasySerialBuilder.createWaitRspPort]** 生成本类对象
 *
 * 接收数据:写入后立即等待数据返回
 * 发送数据:写入一次数据,就阻塞写入时的协程,等待返回一次数据,带有超时限制；
 * 多个协程同时写入将会排队执行上诉过程；
 */
@Suppress("UNUSED")
class EasyWaitRspPort internal constructor(serialPort: SerialPort) :
    BaseEasySerialPort(serialPort) {

    private val mutex by lazy { Mutex() }//同步锁 防止上一次写入过程未结束 下一次就开始

    /**
     * 设置串口每次从数据流中读取的最大字节数；
     * 对于不可读取字节数的串口，必须在调用[writeWaitRsp]或[writeAllWaitRsp]之前调用，否则设置无效；
     * 在请求时不指定接收数据的最大字节数时，将会使用这里配置的字节大小；
     * @param max 指定串口每次从数据流中读取的最大字节数；
     */
    fun setMaxReadSize(max: Int): EasyWaitRspPort {
        customMaxReadSize = max
        return this
    }

    /**
     * 设置串口数据读取的间隔 单位为毫秒；
     * 默认为10毫秒，读取时间越短，CPU的占用会越高，请合理配置此设置；
     * @param interval 间隔时间(毫秒)
     */
    fun setReadInterval(interval: Long): EasyWaitRspPort {
        serialPort.setReadInterval(interval)
        return this
    }

    /**
     * 写入数据 不阻塞等待结果返回
     * @param order 写入的数据
     */
    suspend fun write(order: ByteArray) {
        mutex.withLock { serialPort.write(order) }
    }

    /**
     * 写入数据并等待返回
     * 写入后将阻塞写入的协程,并等待结果返回
     * @param order 写入的数据
     * @param timeOut 每次读取的超时时间,默认200ms
     * @param bufferSize 接收数据的最大字节数，默认为全局配置的最大字节数
     * @return 返回读取到的数据
     */
    suspend fun writeWaitRsp(
        order: ByteArray,
        timeOut: Long = 200,
        bufferSize: Int = customMaxReadSize
    ): WaitResponseBean {
        mutex.withLock {
            return writeTimeOut(order, timeOut, bufferSize)
        }
    }

    /**
     * 写入数据并等待返回
     * 写入后将阻塞写入的协程,并等待结果返回
     */
    suspend fun writeAllWaitRsp(vararg orderList: ByteArray): MutableList<WaitResponseBean> {
        return writeAllWaitRsp(timeOut = 200, bufferSize = customMaxReadSize, orderList = orderList)
    }

    /**
     * 写入数据并等待返回
     * 写入后将阻塞写入的协程,并等待结果返回
     */
    suspend fun writeAllWaitRsp(
        timeOut: Long = 200,
        vararg orderList: ByteArray
    ): MutableList<WaitResponseBean> {
        return writeAllWaitRsp(
            timeOut = timeOut,
            bufferSize = customMaxReadSize,
            orderList = orderList
        )
    }

    /**
     * 写入数据并等待返回
     * 写入后将阻塞写入的协程,并等待结果返回
     * @param timeOut 每次读取的超时时间,默认200ms
     * @param bufferSize 每个请求接收数据的最大字节数，默认为全局配置的最大字节数
     * @param orderList 写入的数据,可同时写入多个
     * @return 返回读取到的数据
     */
    suspend fun writeAllWaitRsp(
        timeOut: Long = 200,
        bufferSize: Int = customMaxReadSize,
        vararg orderList: ByteArray
    ): MutableList<WaitResponseBean> {
        mutex.withLock {
            val rspList = mutableListOf<WaitResponseBean>()
            orderList.forEach { rspList.add(writeTimeOut(it, timeOut, bufferSize)) }
            return rspList
        }
    }

    /**
     * 阻塞读取数据
     * @param orderBytes 要发送的指令
     * @param timeOut 读取超时限定
     * @param bufferSize 接收数据的最大字节数
     * @return 返回读取到的数据以及数据大小
     */
    private suspend fun writeTimeOut(
        orderBytes: ByteArray,
        timeOut: Long,
        bufferSize: Int
    ): WaitResponseBean {
        val pair = if (serialPort.isNoAvailable) noAvailableHandle(orderBytes, timeOut, bufferSize)
        else availableHandle(orderBytes, timeOut, bufferSize)
        if (pair.second > 0) logPortReceiveData(pair.first)//显示读取到的数据的日志
        return WaitResponseBean(pair.first, pair.second)
    }

    //可读取字节数的处理
    private suspend fun availableHandle(
        orderBytes: ByteArray,
        timeOut: Long,
        bufferSize: Int
    ): Pair<ByteArray, Int> {
        val blockByteBuffer = ByteBuffer.allocate(bufferSize)//读取到的数据
        var size = 0//读取到的数据大小
        serialPort.closeRead()//关闭读取
        //监听返回
        serialPort.setReadDataCallBack(object : EasyReadDataCallBack {
            override suspend fun receiveData(bytes: ByteArray) {
                if (size >= bufferSize) return
                if (size + bytes.size > bufferSize) {
                    blockByteBuffer.put(bytes, 0, bufferSize - size)
                    size = bufferSize
                } else {
                    size += bytes.size
                    blockByteBuffer.put(bytes)//将返回的数据加入缓存中
                }
            }
        })
        serialPort.startRead(customMaxReadSize)//开始读取串口返回的数据
        serialPort.write(orderBytes)//开始写入数据
        delay(timeOut)//等待读取结束
        serialPort.closeRead()//关闭读取
        return Pair(blockByteBuffer.array(), size)
    }

    //不可读取字节数的处理
    private suspend fun noAvailableHandle(
        orderBytes: ByteArray,
        timeOut: Long,
        bufferSize: Int
    ): Pair<ByteArray, Int> {
        val blockByteBuffer = ByteBuffer.allocate(bufferSize)//读取到的数据
        var size = 0//读取到的数据大小
        serialPort.setReadDataCallBack(null)//重置监听
        serialPort.startRead(customMaxReadSize)//开始读取串口返回的数据
        //监听返回
        serialPort.setReadDataCallBack(object : EasyReadDataCallBack {
            override suspend fun receiveData(bytes: ByteArray) {
                if (size >= bufferSize) return
                if (size + bytes.size > bufferSize) {
                    blockByteBuffer.put(bytes, 0, bufferSize - size)
                    size = bufferSize
                } else {
                    size += bytes.size
                    blockByteBuffer.put(bytes)//将返回的数据加入缓存中
                }
            }
        })
        serialPort.write(orderBytes)//开始写入数据
        delay(timeOut)//等待读取结束
        serialPort.setReadDataCallBack(null)
        return Pair(blockByteBuffer.array(), size)
    }

    override suspend fun close() {
        mutex.withLock { super.close() }
    }

}