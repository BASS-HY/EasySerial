package com.bass.easySerial.enums

/**
 * @Description 串口停止位定义
 */
@Suppress("unused")
enum class StopBit(val stopBit: Int) {
    BEmpty(-1),

    /**
     * 1位停止位
     */
    B1(1),

    /**
     * 2位停止位
     */
    B2(2);
}