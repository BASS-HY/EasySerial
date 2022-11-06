package com.bass.easySerial.wrapper

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
class EasyKeepReceivePort<CallBackType> internal constructor() :
    BaseEasySerialPort(), EasyReadDataCallBack {

    private val callBackList by lazy { CopyOnWriteArrayList<EasyReceiveCallBack<CallBackType>>() }//监听数据返回
    private var dataHandle: EasyPortDataHandle<CallBackType>? = null//数据处理类
    private val mutex by lazy { Mutex() }//同步锁 控制并发异常
    private var isStart = false//标志是否已经开启了数据监听

    @Suppress("UNCHECKED_CAST")
    //监听串口数据
    override suspend fun receiveData(bytes: ByteArray) {
        CoroutineScope(Dispatchers.IO).launch {//开启顶层协程,不阻塞串口的读取
            logPortReceiveData(bytes)//输出获取到的数据
            dataHandle?.apply {//自定义了数据处理,则处理数据
                val handleData = receivePortData(bytes)
                callBackList.forEach { it.receiveData(handleData) }//处理完成数据后发生给监听者
            } ?: run {//没有自定义数据处理,则直接返回原始数据
                try {
                    callBackList.forEach { it.receiveData(bytes as CallBackType) }
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
    fun addDataCallBack(callBack: suspend (CallBackType) -> Unit): EasyReceiveCallBack<CallBackType> {
        val receiveCallBack = object : EasyReceiveCallBack<CallBackType> {
            override suspend fun receiveData(data: CallBackType) {
                callBack(data)
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
     * 设置串口单次接收数据的最大字节数
     * @param bufferSize 指定串口单次接收数据的最大字节数
     */
    fun setBufferSize(bufferSize: Int): EasyKeepReceivePort<CallBackType> {
        customBufferSize = bufferSize
        return this
    }

    /**
     * 设置串口数据读取的间隔 单位为毫秒；
     * 此方法只在未调用[addDataCallBack]之前设置有效，后续设置不生效；
     * @param interval 间隔时间(毫秒)
     */
    fun setReadInterval(interval: Long): EasyKeepReceivePort<CallBackType> {
        readInterval = interval
        return this
    }

    /**
     * 写入一个数据
     * @param byteArray 写入的数据
     */
    fun write(byteArray: ByteArray) {
        serialPort?.write(byteArray)
    }

    //开始接收数据
    private fun start() {
        if (mutex.isLocked || isStart) return
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                if (isStart) return@launch//已经开启了则不再开启
                serialPort?.let {
                    it.setReadDataCallBack(this@EasyKeepReceivePort)
                    it.startRead(customBufferSize, readInterval)
                    isStart = true
                }
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
