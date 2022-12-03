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

package com.bass.easySerial.handle

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Create by BASS
 * on 2022/6/1 15:33.
 * 数据处理类 继承此类,对串口返回的数据进行处理，将处理后的数据返回给监听者
 */
abstract class EasyPortDataHandle<CallBackType> {

    private val mutex = Mutex()//同步锁,防止处理数据的过程太慢,而输入过快,导致接收端数据紊乱的问题

    internal suspend fun receivePortData(byteArray: ByteArray): List<CallBackType> {
        mutex.withLock { return portData(byteArray) }
    }

    /**
     * 数据处理方法
     * @param byteArray 串口收到的原始数据
     * @return 返回自定义处理后的数据,此数据将被派发到各个监听者
     */
    abstract suspend fun portData(byteArray: ByteArray): List<CallBackType>

    /**
     * 串口关闭时会回调此方法
     * 如果您需要,可重写此方法,在此方法中做释放资源的操作
     */
    open fun close() {}
}