package com.bass.easySerial.enums

/**
 * @Description 串口数据位定义
 */
@Suppress("unused")
enum class DataBit(val dataBit: Int) {
    CSEmpty(-1),

    /**
     * 5位数据位
     */
    CS5(5),

    /**
     * 6位数据位
     */
    CS6(6),

    /**
     * 7位数据位
     */
    CS7(7),

    /**
     * 8位数据位
     */
    CS8(8);
}