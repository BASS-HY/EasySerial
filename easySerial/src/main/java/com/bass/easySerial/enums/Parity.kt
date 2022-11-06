package com.bass.easySerial.enums

/**
 * @Description 串口校验位定义
 */
@Suppress("unused")
enum class Parity(val parity: Int) {
    /**
     * 无奇偶校验
     */
    NONE(0),

    /**
     * 奇校验
     */
    ODD(1),

    /**
     * 偶校验
     */
    EVEN(2);
}