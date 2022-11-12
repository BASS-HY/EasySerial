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

package com.bass.easySerial

import com.bass.easySerial.enums.DataBit
import com.bass.easySerial.extend.*
import com.bass.easySerial.extend.blockWithTimeoutOrNull
import com.bass.easySerial.extend.logPortSendData
import com.bass.easySerial.extend.tryCatch
import com.bass.easySerial.extend.tryCatchSuspend
import com.bass.easySerial.interfaces.EasyReadDataCallBack
import kotlinx.coroutines.*
import java.io.*

/**
 * 串口对象创建类
 * @param device 串口文件
 * @param baudRate 波特率
 * @param flags 校验位
 * @param dataBits 数据位 取值 5、6、7、8
 * @param stopBits 停止位 取值1 或者 2
 * @param parity 校验类型 取值 0(NONE), 1(ODD), 2(EVEN)
 * @param flowCon 流控
 */
class SerialPort(
    device: File, baudRate: Int, flags: Int,
    dataBits: Int, stopBits: Int, parity: Int, flowCon: Int = 0
) {
    private val mFd: FileDescriptor?//文件描述符
    internal val isNoAvailable: Boolean//是否为不可读取字节数的串口
    private val devicePath: String
    private var mFileInputStream: FileInputStream? = null//串口读取流
    private var mFileOutputStream: FileOutputStream? = null//串口写入流
    private var readJob: Job? = null//重复读取的协程
    private var readDataCallBack: EasyReadDataCallBack? = null//串口读取数据观察者
    private var readInterval = 10L//串口数据读取的间隔 单位为毫秒


    companion object {
        init {
            System.loadLibrary("EasySerial")
        }
    }

    init {
        //判断串口名称是否为不可读取字节数的串口
        devicePath = device.absolutePath
        isNoAvailable =
            EasySerialBuilder.noAvailableList.find { it == devicePath }?.let { true } ?: false
        //检查访问权限
        if (!device.canRead() || !device.canWrite()) {
            try {
                /* Missing read/write permission, trying to chmod the file */
                val su: Process = Runtime.getRuntime().exec("/system/bin/su")
                val cmd = "chmod 666 ${device.absolutePath}\nexit\n"
                su.outputStream.write(cmd.toByteArray())
                if (su.waitFor() != 0 || !device.canRead() || !device.canWrite()) throw SecurityException()
            } catch (e: Exception) {
                e.printStackTrace()
                throw SecurityException()
            }
        }
        mFd = if (dataBits == DataBit.CSEmpty.dataBit) open(device.absolutePath, baudRate, flags)
        else open2(device.absolutePath, baudRate, stopBits, dataBits, parity, flowCon, flags)
        if (mFd == null) {
            logE("native open returns null")
            throw IOException()
        }
        mFileInputStream = FileInputStream(mFd)
        mFileOutputStream = FileOutputStream(mFd)
    }

    //开始永久接收
    internal fun startRead(maxReadSize: Int) {
        readJob?.let { return }
        readJob = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ByteArray(maxReadSize)
            var size: Int
            while (isActive) {
                try {
                    mFileInputStream ?: throw NullPointerException("串口输入流未被初始化!!!")
                    mFileInputStream?.let {
                        size = if (isNoAvailable) noAvailableHandle(buffer, it)
                        else availableHandle(buffer, it)
                        //4.判断读取到的字节大小 返回读取到的字节的副本
                        if (size > 0) readDataCallBack?.receiveData(buffer.copyOf(size))
                    }
                    delay(readInterval)
                } catch (e: Exception) {
                    logE(e)
                    readJob = null
                    break
                }
            }
        }
    }

    //关闭永久接收
    internal suspend fun closeRead() {
        withTimeoutOrNull(200) { tryCatchSuspend { readJob?.cancelAndJoin() } }
        readJob = null
        readDataCallBack = null
    }

    //设置串口读取的间隔
    internal fun setReadInterval(readInterval: Long) {
        this.readInterval = readInterval
    }

    //监听串口数据
    internal fun setReadDataCallBack(readDataCallBack: EasyReadDataCallBack?) {
        this.readDataCallBack = readDataCallBack
    }

    //直接写入
    internal fun write(bytes: ByteArray?) {
        logPortSendData(bytes)
        tryCatch { bytes?.let { mFileOutputStream?.write(it) } }
    }

    //关闭串口以及输入输出流
    internal suspend fun closeSerial() {
        closeRead()
        tryCatch { mFileInputStream?.close() }
        tryCatch { mFileOutputStream?.close() }
        close()
    }

    //获取串口的名称 如：/dev/ttyS4
    internal fun getDevicePath(): String = devicePath

    //不允许读取字节数的处理
    private fun noAvailableHandle(buffer: ByteArray, inputStream: FileInputStream): Int {
        return inputStream.read(buffer)
    }

    //允许读取字节数的处理
    private suspend fun availableHandle(buffer: ByteArray, inputStream: FileInputStream): Int {
        //1.获取可读的字节数
        var availableSize = tryCatchSuspend(0) {
            blockWithTimeoutOrNull(5) { inputStream.available() } ?: 0
        }
        //2.判断可读的字节数是否合规
        if (availableSize < 0) availableSize = 0
        else if (availableSize > buffer.size) availableSize = buffer.size
        //3.读取超时限定 读取指定长度的数据,如果不指定长度,在无法读取时可能会阻塞协程
        return blockWithTimeoutOrNull(10) {
            inputStream.read(buffer, 0, availableSize)
        } ?: 0
    }

    private external fun open(path: String, baudRate: Int, flags: Int): FileDescriptor?
    private external fun open2(
        path: String,
        baudRate: Int,
        stopBits: Int,
        dataBits: Int,
        parity: Int,
        flowCon: Int,
        flags: Int
    ): FileDescriptor?

    private external fun close()
}