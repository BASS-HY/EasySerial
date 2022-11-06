package com.bass.easyserialport.keepReceive

import android.util.Log
import com.bass.easySerial.EasySerialBuilder
import com.bass.easySerial.enums.*
import com.bass.easySerial.extend.conver2HexString
import com.bass.easySerial.interfaces.EasyReceiveCallBack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Create by BASS
 * on 2022/10/29 22:24.
 * 演示1：
 * 创建一个永久接收的串口(串口开启失败返回Null)
 * 不做自定义返回数据的处理
 */
@Suppress("unused")
class KeepReceiveDemo1 {

    private val tag = "KeepReceiveDemo1"

    /**
     * 演示1：
     * 创建一个永久接收的串口(串口开启失败返回Null)
     */
    fun createPort() {
        //创建一个串口,串口返回的数据默认为ByteArray类型；
        val tempPort =
            EasySerialBuilder.createKeepReceivePort<ByteArray>("/dev/ttyS4", BaudRate.B4800)

        //我们还可以这样创建串口,串口返回的数据默认为ByteArray类型；
        val tempPort2 = EasySerialBuilder.createKeepReceivePort<ByteArray>(
            "/dev/ttyS4",
            BaudRate.B4800, DataBit.CS8, StopBit.B1,
            Parity.NONE, 0, FlowCon.NONE
        )

        //串口可能会开启失败,在这里做简单判断；
        val port = tempPort ?: return

        //设置单次接收数据的最大字节数,默认为64个字节；
        port.setBufferSize(64)

        //设置数据的读取间隔,即上一次读取完数据后,隔多少秒后读取下一次数据；
        //默认为10毫秒,读取时间越短，CPU的占用会越高,请合理配置此设置；
        port.setReadInterval(100)

        //监听串口返回的数据; 第一种写法；须注意，此回调处于协程之中；
        val dataCallBack1 = port.addDataCallBack {
            //处理项目逻辑；
            // 此处示范将串口数据转化为16进制字符串；
            val hexString = it.conver2HexString()
            Log.d(tag, "接收到串口数据:$hexString")
        }
        //在我们不再需要使用的时候,可以移除串口监听；
        port.removeDataCallBack(dataCallBack1)

        //监听串口返回的数据,第二种写法；须注意，此回调处于协程之中；
        val dataCallBack2 = object : EasyReceiveCallBack<ByteArray> {
            override suspend fun receiveData(data: ByteArray) {
                //处理项目逻辑；
                //此处示范将串口数据转化为16进制字符串；
                val hexString = data.conver2HexString()
                Log.d(tag, "接收到串口数据:$hexString")
            }

        }
        port.addDataCallBack(dataCallBack2)
        //在我们不再需要使用的时候,可以移除串口监听；
        port.removeDataCallBack(dataCallBack2)

        //使用完毕关闭串口，关闭串口须在协程中关闭，关闭时会阻塞当前协程，直到关闭处理完成；
        //这个过程并不会耗费太长时间,一般为1ms-4ms;
        CoroutineScope(Dispatchers.IO).launch { port.close() }
    }

}