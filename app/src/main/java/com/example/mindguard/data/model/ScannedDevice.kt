package com.example.mindguard.data.model

import android.bluetooth.BluetoothDevice

data class ScannedDevice(
    val name : String?,
    val device: BluetoothDevice,
    val data : String,
    var lastSeen: Long
)
