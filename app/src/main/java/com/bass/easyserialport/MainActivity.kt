package com.bass.easyserialport

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bass.easySerial.extend.conver2ByteArray
import com.bass.easySerial.extend.conver2HexString

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}