package com.example.mindguard.data.service

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.AdvertiseData
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mindguard.data.model.ScannedDevice


class BluetoothService(private val context : Context){

    private val packageManager : PackageManager = context.packageManager
    private val bluetoothManager : BluetoothManager? = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private val bluetoothLeScanner : BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner

    private val bluetoothLeAdvertiser : BluetoothLeAdvertiser? = bluetoothAdapter?.bluetoothLeAdvertiser
    private val advertiseSettings = AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY) // Low latency advertisement
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM) // Medium TX power
        .build()

    private val _devices : MutableLiveData<List<ScannedDevice>> = MutableLiveData(mutableListOf())
    val devices : LiveData<List<ScannedDevice>> = _devices
    private val expirationTime = 10000L

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
            }
            val device = result.device
            if (device.name != null){
                Log.d("device name to :",device.name)
            }
            val existingDevice = _devices.value?.find { it.device.address == device.address }

            if (existingDevice != null) {
                existingDevice.lastSeen = System.currentTimeMillis()
            } else {
                addDeviceToList(ScannedDevice(device.name,device, System.currentTimeMillis()))
            }
            cleanUpDeviceList()
        }
    }

    val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            Log.i("adv","Advertising started successfully")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.i("adv","Advertising failed with error code: $errorCode")
        }
    }

    private fun addDeviceToList(device: ScannedDevice) {
        val currentList = _devices.value?.toMutableList() ?: mutableListOf()
        currentList.add(device)
        _devices.value = currentList
    }

    private fun cleanUpDeviceList() {
        val currentTime = System.currentTimeMillis()
        val filteredList = _devices.value?.filter { currentTime - it.lastSeen <= expirationTime } ?: emptyList()
        _devices.value = filteredList
    }

    fun deviceHasBLE() : Boolean{
        return packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    fun startDeviceLeScan() : Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        bluetoothLeScanner?.startScan(leScanCallback)
        return true
    }

    fun stopDeviceLeScan() : Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        bluetoothLeScanner?.stopScan(leScanCallback)
        return true
    }

    fun startLeAdvertise(data : String) : Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        val advertiseData = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid.fromString(data))
            .build()

        bluetoothLeAdvertiser?.startAdvertising(advertiseSettings, advertiseData,advertiseCallback)
        Log.i("adv","Advertising started successfully")
        return true
    }

    fun stopLeAdvertise() : Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        Log.i("adv","Advertising stopped successfully")
        return true
    }


}
