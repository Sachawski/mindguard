package com.example.mindguard.data.repository

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mindguard.data.model.ScannedDevice

class BluetoothRepository(private val context : Context) {


    private val bluetoothManager : BluetoothManager? = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private val bluetoothLeScanner : BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private val bluetoothLeAdvertiser : BluetoothLeAdvertiser? = bluetoothAdapter?.bluetoothLeAdvertiser
    private val advertiseSettings = AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY) // Low latency advertisement
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM) // Medium TX power
        .build()

    private val devices : MutableLiveData<List<ScannedDevice>> = MutableLiveData(mutableListOf())

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
            val existingDevice = devices.value?.find { it.device.address == device.address }
            val payload : Map<ParcelUuid, ByteArray>? = result.scanRecord?.serviceData

            if (payload != null) {
                for (data in payload){
                    val key = data.key.toString() //Service UUID (friend's ID)
                    val value = String(data.value) //Byte array back to string
                    if (value.startsWith("mindguard")){
                        if (existingDevice != null) {
                            existingDevice.lastSeen = System.currentTimeMillis()
                        } else {
                            addDeviceToList(ScannedDevice(device.name,device,key, System.currentTimeMillis()))
                        }
                    }
                }
            }
        }
        override fun onScanFailed(errorCode: Int) {
            when (errorCode) {
                SCAN_FAILED_ALREADY_STARTED -> Log.e("BLEScan", "Scan already started")
                SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> Log.e("BLEScan", "App registration failed")
                SCAN_FAILED_INTERNAL_ERROR -> Log.e("BLEScan", "Internal error occurred")
                SCAN_FAILED_FEATURE_UNSUPPORTED -> Log.e("BLEScan", "Feature not supported")
                else -> Log.e("BLEScan", "Unknown scan error: $errorCode")
            }
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            Log.i("BLE_ADV","Advertising started successfully")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            if (errorCode == ADVERTISE_FAILED_INTERNAL_ERROR){
                Log.e("adv","Internal error: $errorCode")
            }
            if (errorCode == ADVERTISE_FAILED_ALREADY_STARTED){
                Log.e("adv","Already started: $errorCode")
            }
            if (errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE){
                Log.e("adv","Data too large: $errorCode")
            }
            if (errorCode == ADVERTISE_FAILED_FEATURE_UNSUPPORTED){
                Log.e("adv","Feature unsupported: $errorCode")
            }
            if (errorCode == ADVERTISE_FAILED_TOO_MANY_ADVERTISERS){
                Log.e("adv","Too many advertiser: $errorCode")
            }
        }
    }

    private fun addDeviceToList(device: ScannedDevice) {
        val currentList = devices.value?.toMutableList() ?: mutableListOf()
        currentList.add(device)
        devices.value = currentList
    }

    private fun cleanUpDeviceList() {
        val currentTime = System.currentTimeMillis()
        val filteredList = devices.value?.filter { currentTime - it.lastSeen <= expirationTime } ?: emptyList()
        devices.postValue(filteredList)
    }

    fun startScan() : Boolean {
        cleanUpDeviceList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        val scanFilter = ScanFilter.Builder().build() //dummy manufacturer ID in order to trigger the scan. We need to make the app advertise this.
        val scanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        bluetoothLeScanner?.startScan(listOf(scanFilter),scanSettings,leScanCallback)
        return true
    }

    fun stopScan() : Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)  {
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

    fun startAdvertise(data : String) : Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        val advertiseData = AdvertiseData.Builder()
            .addServiceData(ParcelUuid.fromString(data),"mindguard".toByteArray()) // data is mindguard+user's uuid
            .build()

        bluetoothLeAdvertiser?.startAdvertising(advertiseSettings, advertiseData,advertiseCallback)
        return true
    }

    fun stopAdvertise() : Boolean {
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
        return true
    }

    fun getDevices() : LiveData<List<ScannedDevice>> {
        return devices
    }

}