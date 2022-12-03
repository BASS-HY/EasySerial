package com.bass.easyserialport.keepReceive

import com.bass.easySerial.extend.conver2HexString
import com.bass.easySerial.handle.EasyPortDataHandle
import java.util.regex.Pattern

/**
 * Create by BASS
 * on 2022/10/29 23:05.
 * 自定义的数据解析规则；
 */
class CustomEasyPortDataHandle : EasyPortDataHandle<String>() {

    private val stringList = mutableListOf<String>()//用于记录数据
    private val stringBuilder = StringBuilder()//用于记录数据
    private val pattern = Pattern.compile("(AT)(.*?)(\r\n)")//用于匹配数据

    /**
     * 数据处理方法
     *
     * @param byteArray 串口收到的原始数据
     * @return 返回自定义处理后的数据,此数据将被派发到各个监听者
     *
     *
     * 我们可以在这里做很多事情，比如有时候串口返回的数据并不是完整的数据，
     * 它可能有分包返回的情况，我们需要自行凑成一个完整的数据后再返回给监听者，
     * 在数据不完整的时候我们直接返回空数据集给监听者,告知他们这不是一个完整的数据；
     *
     * 在这里我们做个演示,假设数据返回是以AT开头,换行符为结尾的数据是正常的数据；
     *
     */
    override suspend fun portData(byteArray: ByteArray): List<String> {
        //清除之前记录的匹配成功的数据
        stringList.clear()

        //将串口数据转为16进制字符串
        val hexString = byteArray.conver2HexString()
        //记录本次读取到的串口数据
        stringBuilder.append(hexString)

        while (true) {//循环匹配,直到匹配完所有的数据
            //寻找记录中符合规则的数据
            val matcher = pattern.matcher(stringBuilder)
            //没有寻找到符合规则的数据,则返回Null
            if (!matcher.find()) break
            //寻找到符合规则的数据,记录匹配成功的数据,并将其从StringBuilder中删除
            val group = matcher.group()
            stringList.add(group)
            stringBuilder.delete(matcher.start(), matcher.end())
        }

        //返回记录的匹配成功的数据
        return stringList.toList()
    }

    /**
     * 串口关闭时会回调此方法
     * 如果您需要,可重写此方法,在此方法中做释放资源的操作
     */
    override fun close() {
        stringBuilder.clear()
        stringList.clear()
    }
}