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