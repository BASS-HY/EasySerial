package com.bass.easySerial.wrapper

import com.bass.easySerial.bean.WaitResponseBean
import com.bass.easySerial.extend.tryCatch
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
class EasyWaitRspPort internal constructor() : BaseEasySerialPort() {

    private val mutex by lazy { Mutex() }//同步锁 防止上一次写入过程未结束 下一次就开始

    /**
     * 设置串口单次接收数据的最大字节数
     * @param bufferSize 指定串口单次接收数据的最大字节数
     */
    fun setBufferSize(bufferSize: Int): EasyWaitRspPort {
        customBufferSize = bufferSize
        return this
    }

    /**
     * 设置串口数据读取的间隔 单位为毫秒；
     * 对于可读取字节数的串口，此方法可在每次调用[writeWaitRsp]之前设置本次的数据读取间隔；
     * 对于不可读取字节数的串口，此方法只在未调用[writeWaitRsp]或[writeAllWaitRsp]之前设置有效，后续设置不生效；
     * @param interval 间隔时间(毫秒)
     */
    fun setReadInterval(interval: Long): EasyWaitRspPort {
        readInterval = interval
        return this
    }

    /**
     * 写入数据 不阻塞等待结果返回
     * @param order 写入的数据
     */
    suspend fun write(order: ByteArray) {
        mutex.withLock { serialPort?.write(order) }
    }

    /**
     * 写入数据并等待返回
     * 写入后将阻塞写入的协程,并等待结果返回
     * @param order 写入的数据
     * @param timeOut 每次读取的超时时间,默认200ms
     * @return 返回读取到的数据
     */
    suspend fun writeWaitRsp(order: ByteArray, timeOut: Long = 200): WaitResponseBean {
        mutex.withLock {
            return writeTimeOut(order, timeOut)
        }
    }

    /**
     * 写入数据并等待返回
     * 写入后将阻塞写入的协程,并等待结果返回
     * @param timeOut 每次读取的超时时间,默认200ms
     * @param orderList 写入的数据,可同时写入多个
     * @return 返回读取到的数据
     */
    suspend fun writeAllWaitRsp(
        timeOut: Long = 200,
        vararg orderList: ByteArray
    ): MutableList<WaitResponseBean> {
        mutex.withLock {
            val rspList = mutableListOf<WaitResponseBean>()
            orderList.forEach { rspList.add(writeTimeOut(it, timeOut)) }
            return rspList
        }
    }

    /**
     * 阻塞读取数据
     * @param orderBytes 要发送的指令
     * @param timeOut 读取超时限定
     * @return 返回读取到的数据以及数据大小
     */
    private suspend fun writeTimeOut(orderBytes: ByteArray, timeOut: Long): WaitResponseBean {
        val pair = if (serialPort?.isNoAvailable == true) noAvailableHandle(orderBytes, timeOut)
        else availableHandle(orderBytes, timeOut)
        if (pair.second > 0) logPortReceiveData(pair.first)//显示读取到的数据的日志
        return WaitResponseBean(pair.first, pair.second)
    }

    //可读取字节数的处理
    private suspend fun availableHandle(
        orderBytes: ByteArray,
        timeOut: Long
    ): Pair<ByteArray, Int> {
        val blockByteBuffer = ByteBuffer.allocate(customBufferSize)//读取到的数据
        var size = 0//读取到的数据大小
        serialPort?.apply {
            closeRead()//关闭读取
            //监听返回
            setReadDataCallBack(object : EasyReadDataCallBack {
                override suspend fun receiveData(bytes: ByteArray) {
                    tryCatch {
                        size += bytes.size
                        blockByteBuffer.put(bytes)//将返回的数据加入缓存中
                    }
                }
            })
            startRead(customBufferSize, readInterval)//开始读取串口返回的数据
            write(orderBytes)//开始写入数据
            delay(timeOut)//等待读取结束
            closeRead()//关闭读取
        }
        return Pair(blockByteBuffer.array(), size)
    }

    //不可读取字节数的处理
    private suspend fun noAvailableHandle(
        orderBytes: ByteArray,
        timeOut: Long
    ): Pair<ByteArray, Int> {
        val blockByteBuffer = ByteBuffer.allocate(customBufferSize)//读取到的数据
        var size = 0//读取到的数据大小
        serialPort?.apply {
            setReadDataCallBack(null)//重置监听
            startRead(customBufferSize, readInterval)//开始读取串口返回的数据
            //监听返回
            setReadDataCallBack(object : EasyReadDataCallBack {
                override suspend fun receiveData(bytes: ByteArray) {
                    tryCatch {
                        size += bytes.size
                        blockByteBuffer.put(bytes)//将返回的数据加入缓存中
                    }
                }
            })
            write(orderBytes)//开始写入数据
            delay(timeOut)//等待读取结束
            setReadDataCallBack(null)
        }
        return Pair(blockByteBuffer.array(), size)
    }

    override suspend fun close() {
        mutex.withLock { super.close() }
    }

}