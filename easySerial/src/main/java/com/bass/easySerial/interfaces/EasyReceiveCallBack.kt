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

package com.bass.easySerial.interfaces

/**
 * Create by BASS
 * on 2022/8/11 13:42.
 * 永久接收的串口返回处理后的串口数据给监听者
 */
interface EasyReceiveCallBack<CallBackType> {

    /**
     * 返回处理后的串口数据给监听者
     * @param data 处理后的串口数据
     */
    suspend fun receiveData(data: CallBackType)

}