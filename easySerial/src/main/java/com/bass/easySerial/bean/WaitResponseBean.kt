package com.bass.easySerial.bean

/**
 * 写入后等待返回的数据类
 * @param bytes 串口返回的数据
 * @param size 读取到的数据大小
 */
data class WaitResponseBean(val bytes: ByteArray, val size: Int) {
    override fun equals(other: Any?): Boolean {
        if (other !is WaitResponseBean) return false
        if (size != other.size) return false
        if (bytes.size != other.bytes.size) return false
        for (index in bytes.indices) {
            if (bytes[index] != other.bytes[index]) return false
        }
        return true
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}