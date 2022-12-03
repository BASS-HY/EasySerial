package com.bass.easyserialport.keepReceive

import android.util.Log
import com.bass.easySerial.EasySerialBuilder
import com.bass.easySerial.enums.*
import com.bass.easySerial.interfaces.EasyReceiveCallBack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Create by BASS
 * on 2022/10/29 23:00.
 * 演示2：
 * 创建一个永久接收的串口(串口开启失败返回Null)
 * 自定义回调的数据类型
 */
@Suppress("unused")
class KeepReceiveDemo2 {

    private val tag = "KeepReceiveDemo2"

    /**
     * 演示2：
     * 创建一个永久接收的串口(串口开启失败返回Null)
     */
    fun createPort() {
        //创建一个串口,串口返回的数据类型,我们自定义为String类型；
        val tempPort =
            EasySerialBuilder.createKeepReceivePort<String>("/dev/ttyS4", BaudRate.B4800)

        //我们还可以这样创建串口,串口返回的数据类型,我们自定义为String类型；
        val tempPort2 = EasySerialBuilder.createKeepReceivePort<String>(
            "/dev/ttyS4",
            BaudRate.B4800, DataBit.CS8, StopBit.B1,
            Parity.NONE, 0, FlowCon.NONE
        )

        //串口可能会开启失败,在这里做简单判断；
        val port = tempPort ?: return

        //设置串口每次从数据流中读取的最大字节数,默认为64个字节；
        //注意：此方法一定要在监听串口返回之前设置，否则设置无效；
        port.setMaxReadSize(64)

        //设置数据的读取间隔,即上一次读取完数据后,隔多少秒后读取下一次数据；
        //默认为10毫秒,读取时间越短，CPU的占用会越高,请合理配置此设置；
        port.setReadInterval(100)

        //因为我们设置数据返回类型不再是默认的ByteArray类型，所以我们需要设置自定义的数据解析规则；
        port.setDataHandle(CustomEasyPortDataHandle())

        //监听串口返回的数据; 第一种写法；须注意，此回调处于协程之中；
        val dataCallBack1 = port.addDataCallBack {
            //返回的数据集内没有数据,则表明没有匹配成功的数据；
            //我们这里不处理没有匹配成功的情况；
            if (it.isEmpty()) return@addDataCallBack
            //处理项目逻辑；
            //此处演示直接将转化后的数据类型打印出来；
            Log.d(tag, "接收到串口数据:$it")
        }
        //在我们不再需要使用的时候,可以移除串口监听；
        port.removeDataCallBack(dataCallBack1)

        //监听串口返回的数据,第二种写法；须注意，此回调处于协程之中；
        val dataCallBack2 = object : EasyReceiveCallBack<String> {
            override suspend fun receiveData(dataList: List<String>) {
                //返回的数据集内没有数据,则表明没有匹配成功的数据；
                //我们这里不处理没有匹配成功的情况；
                if (dataList.isEmpty()) return
                //处理项目逻辑；
                //此处演示直接将转化后的数据类型打印出来；
                dataList.forEach { Log.d(tag, "接收到串口数据:$it") }
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