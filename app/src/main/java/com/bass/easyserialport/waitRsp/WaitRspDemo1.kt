package com.bass.easyserialport.waitRsp

import android.util.Log
import com.bass.easySerial.EasySerialBuilder
import com.bass.easySerial.enums.*
import com.bass.easySerial.extend.conver2ByteArray
import com.bass.easySerial.extend.conver2HexString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Create by BASS
 * on 2022/10/30 0:21.
 * 演示1：
 * 创建一个发送后再接收的串口(串口开启失败返回Null)
 */
@Suppress("unused")
class WaitRspDemo1 {

    private val tag = "WaitRspDemo1"

    fun createPort() {
        //创建一个发送后再接收的串口
        val tempPort = EasySerialBuilder.createWaitRspPort("/dev/ttyS4", BaudRate.B4800)

        //我们还可以这样创建串口
        val tempPort2 = EasySerialBuilder.createWaitRspPort(
            "/dev/ttyS4",
            BaudRate.B4800, DataBit.CS8, StopBit.B1,
            Parity.NONE, 0, FlowCon.NONE
        )

        //串口可能会开启失败,在这里做简单判断；
        val port = tempPort ?: return

        //设置单词接收数据的最大字节数,默认为64个字节；
        port.setBufferSize(64)

        //设置数据的读取间隔,即上一次读取完数据后,隔多少秒后读取下一次数据；
        //默认为10毫秒,读取时间越短，CPU的占用会越高,请合理配置此设置；
        port.setReadInterval(100)

        //假设几个串口命令:
        val orderByteArray1 = "1A FF FF".conver2ByteArray()
        val orderByteArray2 = "2B FF FF".conver2ByteArray()
        val orderByteArray3 = "3C FF FF".conver2ByteArray()

        //发送串口命令,并等待返回的示例1：
        CoroutineScope(Dispatchers.IO).launch {
            //此方法我们必须在协程作用域中调用，默认等待返回时间为200ms,
            //即调用此函数,将会阻塞200ms,并将此期间接收到的串口数据返回给调用方；
            val rspBean = port.writeWaitRsp(orderByteArray1)
            //此外，我们也可以指定等待时间,如下示例:
            val rspBean2 = port.writeWaitRsp(orderByteArray1, 500)

            //讲解一下返回的数据：
            rspBean.bytes
            //串口返回的数据,此字节数组的大小为我们setBufferSize()时输入的字节大小；
            //需要注意的是,字节数组内的字节并不全是串口返回的数据；
            //我们假设串口返回了4个字节的数据，那么其余的60个字节都是0；
            //那我们怎么知道收到了多少个字节呢？
            rspBean.size
            //以上便是串口返回的字节长度,所以我们取串口返回的实际字节数组可以这样取：
            val portBytes = rspBean.bytes.copyOf(rspBean.size)
            //插句题外话，我们也提供了直接将读取到的字节转为16进制字符串的方法:
            val hexString = rspBean.bytes.conver2HexString(rspBean.size)

            //在获取到后做我们自己的业务逻辑
            Log.d(tag, "接收到数据:${hexString}")
        }

        //发送串口命令,并等待返回的示例2：
        //有时候，我们可能需要连续向串口输出命令，并等待其返回,对此我们也提供了便捷的方案:
        CoroutineScope(Dispatchers.IO).launch {
            //此方法我们必须在协程作用域中调用
            //调用此方法我们内部将按照顺序一个一个请求并收集结果,将结果返回
            val rspBeanList =
                port.writeAllWaitRsp(200, orderByteArray1, orderByteArray2, orderByteArray3)
            //以下返回的数据与请求一一对应：
            val rspBean1 = rspBeanList[0]//orderByteArray1
            val rspBean2 = rspBeanList[1]//orderByteArray2
            val rspBean3 = rspBeanList[2]//orderByteArray3

            //在获取到后做我们自己的业务逻辑
            Log.d(tag, "接收到数据:${rspBean1.bytes.conver2HexString(rspBean1.size)}")
            Log.d(tag, "接收到数据:${rspBean2.bytes.conver2HexString(rspBean2.size)}")
            Log.d(tag, "接收到数据:${rspBean3.bytes.conver2HexString(rspBean3.size)}")
        }

        //发送串口命令,示例3：
        //在同一个串口中,我们有些需要等待串口的数据返回,有些是不需要的,在不需要串口数据返回的情况下，
        //我们可以直接调用写入即可:
        CoroutineScope(Dispatchers.IO).launch {
            //此方法我们必须在协程作用域中调用
            port.write(orderByteArray1)
        }

        //使用完毕关闭串口，关闭串口须在协程中关闭，关闭时会阻塞当前协程，直到关闭处理完成；
        //这个过程并不会耗费太长时间,一般为1ms-4ms;
        CoroutineScope(Dispatchers.IO).launch { port.close() }
    }

}