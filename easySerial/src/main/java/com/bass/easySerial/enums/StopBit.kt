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