package com.example.mindguard.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.mindguard.data.model.ScannedDevice
import com.example.mindguard.data.service.BluetoothService

class BluetoothRepository(private val context : Context) {

    private val bluetoothService : BluetoothService = BluetoothService(context)

    fun deviceHasBLE() : Boolean {
        return bluetoothService.deviceHasBLE()
    }

    fun getDevices() : LiveData<List<ScannedDevice>> {
        return bluetoothService.devices
    }

    fun startScan() : Boolean {
        return bluetoothService.startDeviceLeScan()
    }

    fun stopScan() : Boolean {
        return bluetoothService.stopDeviceLeScan()
    }

    fun startAdvertise(data : String) : Boolean {
        return bluetoothService.startLeAdvertise(data)
    }

    fun stopAdvertise() : Boolean {
        return bluetoothService.stopLeAdvertise()
    }

}