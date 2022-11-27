/*
 *   Copyright 2022 BASS
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

@file:Suppress("unused", "SpellCheckingInspection")

package com.bass.easySerial.extend

/**
 * 字符串转16进制字节数组
 * 从字符串第0位截取到[endIndex]位,将其转化位字节数组后返回
 * @param endIndex 默认为字符串的最后一个字符的下标
 * @return 16进制字符串的字节数组
 */
fun String.conver2ByteArray(endIndex: Int = length): ByteArray {
    if (isEmpty() || endIndex < 2) return ByteArray(0)
    //移除空格
    val hexStr = substring(0, if (endIndex != length) endIndex + 1 else endIndex).replace(" ", "")
    if (hexStr.isEmpty() || hexStr.length % 2 != 0) return ByteArray(0)
    //开始转化
    val byteArray = ByteArray(hexStr.length / 2)
    for (i in byteArray.indices) {
        val subStr = hexStr.substring(2 * i, 2 * i + 2)
        byteArray[i] = subStr.toInt(16).toByte()
    }
    return byteArray
}

/**
 * 字符串转16进制字节数组
 * 从字符串第[startIndex]位截取到[endIndex]位,将其转化位字节数组后返回
 * @param startIndex 转化的字符串的起始位置下标
 * @param endIndex  转化的字符串的结束位置下标
 * @return 16进制字符串的字节数组
 */
fun String.conver2ByteArray(startIndex: Int, endIndex: Int): ByteArray {
    if (isEmpty() || startIndex == endIndex || startIndex < 0) return ByteArray(0)
    //移除空格
    val hexStr = substring(startIndex, endIndex + 1).replace(" ", "")
    if (hexStr.isEmpty() || hexStr.length % 2 != 0) return ByteArray(0)
    //开始转化
    val byteArray = ByteArray(hexStr.length / 2)
    for (i in byteArray.indices) {
        val subStr = hexStr.substring(2 * i, 2 * i + 2)
        byteArray[i] = subStr.toInt(16).toByte()
    }
    return byteArray
}


/**
 * 将buteArray转化为16进制的String
 * 每个16进制数之间用空格分割
 * @param size 要转化的字节长度 默认为全部转化
 * @param isUppercase 是否开启大写 默认开启
 */
fun ByteArray.conver2HexStringWithBlank(
    size: Int = this.size,
    isUppercase: Boolean = true
): String {
    if (size == 0) return ""
    val builder = StringBuilder()
    for (index in 0 until size) {
        val hexString = Integer.toHexString(this[index].toInt() and 0xFF)
        val coverHex =
            if (hexString.length == 1) "0${if (isUppercase) hexString.uppercase() else hexString}${if (index == size - 1) "" else " "}"
            else "${if (isUppercase) hexString.uppercase() else hexString}${if (index == size - 1) "" else " "}"
        builder.append(coverHex)
    }
    return builder.toString()
}

/**
 * 将buteArray转化为16进制的String
 * 每个16进制数之间用空格分割
 * @param startIndex 读取的字节数组的起始下标
 * @param endIndex 读取的字节数组的结束下标
 * @param isUppercase 是否开启大写 默认开启
 */
fun ByteArray.conver2HexStringWithBlank(
    startIndex: Int,
    endIndex: Int,
    isUppercase: Boolean = true
): String {
    if (isEmpty() || startIndex < 0 || endIndex < 0) return ""
    val builder = StringBuilder()
    for (index in startIndex..endIndex) {
        val hexString = Integer.toHexString(this[index].toInt() and 0xFF)
        val coverHex =
            if (hexString.length == 1) "0${if (isUppercase) hexString.uppercase() else hexString}${if (index == endIndex) "" else " "}"
            else "${if (isUppercase) hexString.uppercase() else hexString}${if (index == endIndex) "" else " "}"
        builder.append(coverHex)
    }
    return builder.toString()
}


/**
 * 将byteArray转化为16进制的String
 * @param size 要转化的字节长度 默认为全部转化
 * @param isUppercase 是否开启大写 默认开启
 */
fun ByteArray.conver2HexString(size: Int = this.size, isUppercase: Boolean = true): String {
    if (size == 0) return ""
    val builder = StringBuilder()
    for (index in 0 until size) {
        val hexString = Integer.toHexString(this[index].toInt() and 0xFF)
        val coverHex =
            if (hexString.length == 1) "0${if (isUppercase) hexString.uppercase() else hexString}"
            else if (isUppercase) hexString.uppercase()
            else hexString
        builder.append(coverHex)
    }
    return builder.toString()
}

/**
 * 将buteArray转化为16进制的String
 * @param startIndex 读取的字节数组的起始下标
 * @param endIndex 读取的字节数组的结束下标
 * @param isUppercase 是否开启大写 默认开启
 */
fun ByteArray.conver2HexString(
    startIndex: Int,
    endIndex: Int,
    isUppercase: Boolean = true
): String {
    if (isEmpty() || startIndex < 0 || endIndex < 0) return ""
    val builder = StringBuilder()
    for (index in startIndex..endIndex) {
        val hexString = Integer.toHexString(this[index].toInt() and 0xFF)
        val coverHex =
            if (hexString.length == 1) "0${if (isUppercase) hexString.uppercase() else hexString}"
            else if (isUppercase) hexString.uppercase()
            else hexString
        builder.append(coverHex)
    }
    return builder.toString()
}

/**
 * 将buteArray转化为CharArray
 * @param size 要转化的字节长度 默认为全部转化
 */
fun ByteArray.conver2CharArray(size: Int = this.size): CharArray {
    if (size <= 0) return CharArray(0)
    return CharArray(size) { this[it].toInt().toChar() }
}

/**
 * 将buteArray转化为CharArray
 * @param startIndex 读取的字节数组的起始下标
 * @param endIndex 读取的字节数组的结束下标
 */
fun ByteArray.conver2CharArray(startIndex: Int, endIndex: Int): CharArray {
    if (isEmpty() || startIndex < 0 || endIndex < 0) return CharArray(0)
    val size = (endIndex - startIndex) + 1
    return CharArray(size) { this[it + startIndex].toInt().toChar() }
}

/**
 * byteArray计算CRC值
 */
fun ByteArray.getCRC(size: Int = this.size): Int {
    // 预置 1 个 16 位的寄存器为十六进制0xFFFF, 称此寄存器为 CRC寄存器。
    var crc = 0xFFFF
    for (i in 0 until size) {
        // 把第一个 8 位二进制数据 与 16 位的 CRC寄存器的低 8 位相异或, 把结果放于 CRC寄存器
        crc = crc and 0xFF00 or (crc and 0x00FF) xor (this[i].toInt() and 0xFF)
        repeat(8) {
            // 把 CRC 寄存器的内容右移一位( 朝低位)用 0 填补最高位, 并检查右移后的移出位
            if (crc and 0x0001 > 0) {// 如果移出位为 1, CRC寄存器与多项式A001进行异或
                crc = crc shr 1
                crc = crc xor 0xA001
            } else crc = crc shr 1// 如果移出位为 0,再次右移一位
        }
    }
    return crc
}

/**
 * byteArray计算CRC值
 */
fun ByteArray.getCRC(startIndex: Int, endIndex: Int): Int {
    if (isEmpty() || startIndex < 0 || endIndex < 0) throw RuntimeException("计算CRC时发生错误,请检查参数")
    // 预置 1 个 16 位的寄存器为十六进制0xFFFF, 称此寄存器为 CRC寄存器。
    var crc = 0xFFFF
    for (i in startIndex..endIndex) {
        // 把第一个 8 位二进制数据 与 16 位的 CRC寄存器的低 8 位相异或, 把结果放于 CRC寄存器
        crc = crc and 0xFF00 or (crc and 0x00FF) xor (this[i].toInt() and 0xFF)
        repeat(8) {
            // 把 CRC 寄存器的内容右移一位( 朝低位)用 0 填补最高位, 并检查右移后的移出位
            if (crc and 0x0001 > 0) {// 如果移出位为 1, CRC寄存器与多项式A001进行异或
                crc = crc shr 1
                crc = crc xor 0xA001
            } else crc = crc shr 1// 如果移出位为 0,再次右移一位
        }
    }
    return crc
}