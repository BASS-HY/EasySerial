package com.bass.easySerial.bean

import java.io.File
import java.util.*

/**
 * Create by BASS
 * on 2022/6/29 14:07.
 */
internal data class DriverBean(val driverName: String, val deviceRoot: String) {
    val devices by lazy {
        Vector<File>().apply {
            val dev = File("/dev")
            val files = dev.listFiles() ?: return@apply
            files.forEach {
                if (it.absolutePath.startsWith(deviceRoot, true)) add(it)
            }
        }
    }
}