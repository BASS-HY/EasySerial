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