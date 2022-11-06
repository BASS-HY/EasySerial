package com.bass.easySerial.util

import com.bass.easySerial.bean.DriverBean
import com.bass.easySerial.extend.logD
import com.bass.easySerial.extend.tryCatch
import java.io.File
import java.io.FileReader
import java.io.LineNumberReader
import java.util.*

/**
 * Create by BASS
 * on 2022/6/29 14:05.
 */
@Suppress("unused")
object EasySerialFinderUtil {

    /**
     * 获取所有的串口号
     */
    fun getAllDevicesPath(): MutableList<String> {
        val devices = Vector<String>()
        // Parse each driver
        val iterator = getDrivers().iterator()
        tryCatch {
            while (iterator.hasNext()) {
                val bean = iterator.next()
                val fileIterator: Iterator<File> = bean.devices.iterator()
                while (fileIterator.hasNext()) {
                    val device = fileIterator.next().absolutePath
                    devices.add(device)
                }
            }
        }
        return devices.toMutableList()
    }

    fun getAllDevices(): MutableList<String> {
        val devices = Vector<String>()
        tryCatch {
            val iterator = getDrivers().iterator()
            while (iterator.hasNext()) {
                val driver = iterator.next()
                val driverIterator: Iterator<File> = driver.devices.iterator()
                while (driverIterator.hasNext()) {
                    val device = driverIterator.next().name
                    val value = String.format("%s (%s)", device, driver.driverName)
                    devices.add(value)
                }
            }
        }
        return devices.toMutableList()
    }

    private fun getDrivers() = Vector<DriverBean>().apply {
        tryCatch {
            val reader = LineNumberReader(FileReader("/proc/tty/drivers"))
            val regex = Regex(" +")
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                // Since driver name may contain spaces, we do not extract driver name with split()
                val driverName = line!!.substring(0, 0x15).trim { it <= ' ' }
                val typeArray = line!!.split(regex).toTypedArray()
                if (typeArray.size >= 5 && typeArray[typeArray.size - 1] == "serial") {
                    logD("Found new driver " + driverName + " on " + typeArray[typeArray.size - 4])
                    add(DriverBean(driverName, typeArray[typeArray.size - 4]))
                }
            }
            reader.close()
        }
    }
}