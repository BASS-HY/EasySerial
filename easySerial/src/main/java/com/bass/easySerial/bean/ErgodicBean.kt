package com.bass.easySerial.bean

/**
 * Create by BASS
 * on 2021/12/24 16:15.
 */
internal data class ErgodicBean(
    val path: String,
    val baudRate: Int,
    val flags: Int,
    val dataBits: Int = -1,
    val stopBits: Int = -1,
    val parity: Int = 0
)