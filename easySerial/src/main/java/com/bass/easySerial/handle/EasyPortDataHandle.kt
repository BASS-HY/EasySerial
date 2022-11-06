package com.bass.easySerial.handle

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Create by BASS
 * on 2022/6/1 15:33.
 * 数据处理类 继承此类,对串口返回的数据进行处理，将处理后的数据返回给监听者
 */
abstract class EasyPortDataHandle<CallBackType> {

    private val mutex = Mutex()//同步锁,防止处理数据的过程太慢,而输入过快,导致接收端数据紊乱的问题

    internal suspend fun receivePortData(byteArray: ByteArray): CallBackType {
        mutex.withLock {
            return portData(byteArray)
        }
    }

    /**
     * 数据处理方法
     * @param byteArray 串口收到的原始数据
     * @return 返回自定义处理后的数据,此数据将被派发到各个监听者
     */
    abstract suspend fun portData(byteArray: ByteArray): CallBackType

    /**
     * 串口关闭时会回调此方法
     * 如果您需要,可重写此方法,在此方法中做释放资源的操作
     */
    open fun close() {}
}