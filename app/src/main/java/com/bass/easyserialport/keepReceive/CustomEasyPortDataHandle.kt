package com.bass.easyserialport.keepReceive

import com.bass.easySerial.extend.conver2HexString
import com.bass.easySerial.handle.EasyPortDataHandle
import java.util.regex.Pattern

/**
 * Create by BASS
 * on 2022/10/29 23:05.
 * 自定义的数据解析规则；
 */
class CustomEasyPortDataHandle : EasyPortDataHandle<String?>() {

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
     * 在数据不完整的时候我们直接返回Null给监听者,告知他们这不是一个完整的数据；
     *
     * 在这里我们做个演示,假设数据返回是以AT开头,换行符为结尾的数据是正常的数据；
     *
     */
    override suspend fun portData(byteArray: ByteArray): String? {
        //将串口数据转为16进制字符串
        val hexString = byteArray.conver2HexString()
        //记录本次读取到的串口数据
        stringBuilder.append(hexString)
        //寻找记录中符合规则的数据
        val matcher = pattern.matcher(stringBuilder)
        //没有寻找到符合规则的数据,则返回Null
        if (!matcher.find()) return null
        //寻找到符合规则的数据,将其从记录中删除,并返回数据
        val group = matcher.group()
        stringBuilder.delete(matcher.start(), matcher.end())
        return group
    }

    /**
     * 串口关闭时会回调此方法
     * 如果您需要,可重写此方法,在此方法中做释放资源的操作
     */
    override fun close() {
        stringBuilder.clear()
    }
}