package com.bass.easySerial.interfaces

/**
 * 接收串口读取的数据
 */
internal interface EasyReadDataCallBack {

    /**
     * 串口返回的数据
     * @param bytes 为串口读取到的数据的副本
     */
    suspend fun receiveData(bytes: ByteArray)

}