package com.bass.easyserialport.other

import android.util.Log
import com.bass.easySerial.EasySerialBuilder
import com.bass.easySerial.enums.BaudRate
import com.bass.easySerial.extend.conver2ByteArray
import com.bass.easySerial.extend.conver2CharArray
import com.bass.easySerial.extend.conver2HexString
import com.bass.easySerial.extend.conver2HexStringWithBlank
import com.bass.easySerial.util.EasySerialFinderUtil
import com.bass.easySerial.wrapper.BaseEasySerialPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Create by BASS
 * on 2022/10/31 23:42.
 * 其他API的调用Demo
 */
class OtherApiDemo {

    private val tag = "OtherApiDemo"

    /**
     * 获取串口对象，示例
     */
    fun getPortDemo() {
        //1.获取串口对象
        //每一个串口只会创建一个实例，我们在内部缓存了串口实例，即一处创建,到处可取；
        //如果此串口还未创建,则将获取到Null;
        val tempPort: BaseEasySerialPort? = EasySerialBuilder.get("dev/ttyS4")

        val serialPort = tempPort ?: return

        //获取到实例后,我们仅可以调用close()方法关闭串口
        //此方法必须在协程作用域中调用
        CoroutineScope(Dispatchers.IO).launch { serialPort.close() }

        //如果你明确知道当前串口属于哪种类型,那么你可以进行类型强转后使用更多特性。如:
        val easyWaitRspPort = serialPort.cast2WaitRspPort()
        CoroutineScope(Dispatchers.IO).launch {
            val rspBean = easyWaitRspPort.writeWaitRsp("00 FF AA".conver2ByteArray())
        }
        //或者是：
        val keepReceivePort = serialPort.cast2KeepReceivePort<ByteArray>()
        keepReceivePort.write("00 FF AA".conver2ByteArray())
    }

    /**
     * 设置串口不读取字节数，示例：
     */
    fun addNoAvailableDemo() {
        //如果你发现，串口无法收到数据，但是可正常写入数据，使用串口调试工具可正常收发，
        //那么你应当试试如下将串口设置为无法读取字节数：
        EasySerialBuilder.addNoAvailableDevicePath("dev/ttyS4")
        //设置完后再开启串口,否则设置不生效；
        //也可以直接这么写：
        EasySerialBuilder.addNoAvailableDevicePath("dev/ttyS4")
            .createWaitRspPort("dev/ttyS4", BaudRate.B4800)

        //对于`addNoAvailableDevicePath()`方法，需要讲解一下内部串口数据读取的实现了，
        //在读取数据时，会先调用`inputStream.available()` 来判断流中有多少个可读字节，但在部分串口中，即使有数据，
        //`available()`读取到的依旧是0，这就导致了无法读取到数据的情况，当调用`addNoAvailableDevicePath()`后，
        //我们将不再判断流中的可读字节数，而是直接调用`inputStream.read()`方法；
        //当你使用此方法后，请勿重复开启\关闭串口， 因为这样可能会导致串口无法再工作；
    }

    /**
     * 串口日志打印开关
     */
    fun showLogDemo() {
        //是否打印串口通信日志 true为打印日志,false为不打印；
        //建议在Release版本中不打印串口日志；
        //打印的日志的 tag = "EasyPort"；
        EasySerialBuilder.isShowLog(true)
    }

    /**
     * 获取本设备所有的串口名称，示例
     */
    fun getAllPortNameDemo() {
        val allDevicesPath: MutableList<String> = EasySerialFinderUtil.getAllDevicesPath()
        allDevicesPath.forEach {
            Log.d(tag, "串口名称: $it")
        }
    }

    /**
     * 判断当前是否有串口正在使用，示例
     */
    fun hasPortWorkingDemo() {
        val hasPortWorking: Boolean = EasySerialBuilder.hasPortWorking()
        Log.d(tag, "当前是否有串口正在使用: $hasPortWorking")
    }

    /**
     * 数据转化，示例
     */
    fun conversionDemo() {
        /** ----------- 16进制字符串转为字节数组 start------------------*/
        val hexString = "00 FF CA FA"
        //将16进制字符串 转为 字节数组
        val hexByteArray1 = hexString.conver2ByteArray()
        //将16进制字符串从第0位截取到第4位("00 FF") 转为 字节数组
        val hexByteArray2 = hexString.conver2ByteArray(4)
        //将16进制字符串从第2位截取到第4位(" FF") 转为 字节数组
        val hexByteArray3 = hexString.conver2ByteArray(2, 4)
        /** ----------- 16进制字符串转为字节数组  end ------------------*/


        /** ----------- 字节数组转为16进制字符串 start------------------*/
        val byteArray = byteArrayOf(0, -1, 10)// 此字节数组=="00FF0A"
        //将字节数组 转为 16进制字符串
        val hexStr1 = byteArray.conver2HexString()//结果为:"00FF0A"
        //将字节数组取1位 转为 16进制字符串
        val hexStr2 = byteArray.conver2HexString(1)//结果为:"00"
        //将字节数组取2位 转为 16进制字符串 并设置字母为小写
        val hexStr3 = byteArray.conver2HexString(2, false)//结果为:"00ff"
        //将字节数组取第2位到第3位 转为 16进制字符串 并设置字母为小写
        val hexStr4 = byteArray.conver2HexString(1, 2, false)//结果为:"ff0a"
        //将字节数组取第0位 转为 16进制字符串 并设置字母为小写
        val hexStr5 = byteArray.conver2HexString(0, 0, false)//结果为:"00"

        //将字节数组 转为 16进制字符串 16进制之间用空格分隔
        val hexStr6 = byteArray.conver2HexStringWithBlank()//结果为:"00 FF 0A"
        //将字节数组取2位 转为 16进制字符串 16进制之间用空格分隔
        val hexStr7 = byteArray.conver2HexStringWithBlank(2)//结果为:"00 FF"
        //将字节数组取2位 转为 16进制字符串 并设置字母为小写
        val hexStr8 = byteArray.conver2HexStringWithBlank(2, false)//结果为:"00 ff"
        //将字节数组取第2位到第3位 转为 16进制字符串 并设置字母为小写
        val hexStr9 = byteArray.conver2HexStringWithBlank(1, 2, false)//结果为:"ff 0a"
        //将字节数组取第2位 转为 16进制字符串 并设置字母为小写
        val hexStr10 = byteArray.conver2HexStringWithBlank(1, 1, false)//结果为:"ff"
        /** ----------- 字节数组转为16进制字符串  end ------------------*/


        /** -----------   字节数组转为字符数组   start------------------*/
        val byteArray2 =
            byteArrayOf('H'.code.toByte(), 'A'.code.toByte(), 'H'.code.toByte(), 'A'.code.toByte())
        //将字节数组 转为 字符数组
        val charArray1 = byteArray2.conver2CharArray()//即:"HAHA"
        //将字节数组取1位 转为 字符数组
        val charArray2 = byteArray2.conver2CharArray(1)//即:"H"
        //将字节数组取第2位到第3位 转为 字符数组
        val charArray3 = byteArray2.conver2CharArray(2, 3)//即:"HA"
        //将字节数组第2位 转为 字符数组
        val charArray4 = byteArray2.conver2CharArray(2, 2)//即:"H"
    }

}