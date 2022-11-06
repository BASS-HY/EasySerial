package com.bass.easySerial.enums

/**
 * @Description 串口流控定义
 */
@Suppress("unused")
enum class FlowCon(val flowCon: Int) {
    /**
     * 不使用流控
     */
    NONE(0),

    /**
     * 硬件流控
     */
    HARD(1),

    /**
     * 软件流控
     */
    SOFT(2);
}