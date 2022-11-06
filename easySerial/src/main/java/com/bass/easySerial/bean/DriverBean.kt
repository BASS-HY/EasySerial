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