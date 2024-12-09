package com.example.mindguard.data.service

import android.Manifest
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.mindguard.data.model.State
import com.example.mindguard.data.repository.BluetoothRepository
import com.example.mindguard.data.repository.UserRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File

class BackgroundService() : Service()  {

    private var filePath : File = File("")
    private lateinit var userRepository : UserRepository
    private lateinit var bluetoothRepository : BluetoothRepository
    private lateinit var midnightTaskManager: MidnightTaskManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var friendThread : HandlerThread
    private lateinit var friendHandler : Handler

    private lateinit var bluetoothThread : HandlerThread
    private lateinit var bluetoothHandler : Handler

    private lateinit var screenTimeThread : HandlerThread
    private lateinit var screenTimeHandler : Handler

    private lateinit var locationThread : HandlerThread
    private lateinit var locationHandler : Handler

    private var tempWithoutScreenStartTime : Long = System.currentTimeMillis()
    private var tempWithoutScreenEndTime : Long = System.currentTimeMillis()
    private var tempScreenStartTime : Long = System.currentTimeMillis()
    private var tempScreenEndTime : Long = System.currentTimeMillis()

    private var isAtWork = false
    private var isWithFriends = false
    private val scanInterval: Long = 10000

    override fun onCreate() {
        super.onCreate()
        filePath = applicationContext.filesDir
        userRepository = UserRepository(filePath)
        bluetoothRepository = BluetoothRepository(applicationContext)
        midnightTaskManager = MidnightTaskManager(applicationContext)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        friendThread = HandlerThread("Friend Thread")
        friendThread.start()
        friendHandler = Handler(friendThread.looper)

        bluetoothThread = HandlerThread("Bluetooth Thread")
        bluetoothThread.start()
        bluetoothHandler = Handler(bluetoothThread.looper)

        screenTimeThread = HandlerThread("Screen time Thread")
        screenTimeThread.start()
        screenTimeHandler = Handler(screenTimeThread.looper)

        locationThread = HandlerThread("Friend Thread")
        locationThread.start()
        locationHandler = Handler(locationThread.looper)

        startLocationService()
        scheduleStartScan()
        scheduleStartAdvertise()
        scheduleCheckForFriends()
        //periodicCheckForWorkplace()
        launchingScreenTimeRecorder()
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothHandler.removeCallbacksAndMessages(null)
        screenTimeHandler.removeCallbacksAndMessages(null)
        friendHandler.removeCallbacksAndMessages(null)
        bluetoothThread.quitSafely()
        screenTimeThread.quitSafely()
        friendThread.quitSafely()

    }

    override fun onBind(intent: Intent?):    IBinder? {
        return null
    }

    private fun launchingScreenTimeRecorder() {
        screenTimeHandler.post {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            }

            val screenStateReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    when (intent.action) {
                        Intent.ACTION_SCREEN_ON -> {
                            tempScreenStartTime = System.currentTimeMillis()
                        }
                        Intent.ACTION_SCREEN_OFF -> {
                            tempScreenEndTime = System.currentTimeMillis()
                            val timePair = Pair(tempScreenStartTime,tempScreenEndTime)
                            UserRepository.addScreenTime(timePair,UserRepository.user.value!!.getState())
                            //UserRepository.user.value!!.addScreenTime(timePair,_user.value!!.getState())
                            userRepository.saveUser()
                            //_user = UserRepository.user as MutableLiveData<User>
                        }
                    }
                    Log.d("screen time",UserRepository.user.value!!.getScreenTimeInfos().toString())
                    //Log.d("screen time",_user.value!!.getScreenTimeInfos().toString())
                }
            }
            application.registerReceiver(screenStateReceiver, filter)
            midnightTaskManager.scheduleMidnightTask()
        }
    }

    private val startScanRunnable = Runnable {
        Log.d("devices startscan : ",bluetoothRepository.getDevices().value.toString())
        bluetoothRepository.startScan()
        scheduleStopScan()
    }

    private val stopScanRunnable = Runnable {
        Log.d("devices stopscan: ",bluetoothRepository.getDevices().value.toString())
        bluetoothRepository.stopScan()
        scheduleStartScan()
    }

    private val startAdvertiseRunnable = Runnable {
        UserRepository.user.value!!.getId().let { bluetoothRepository.startAdvertise(it) }
        scheduleStopAdvertise()
    }

    private val stopAdvertiseRunnable = Runnable {
        bluetoothRepository.stopAdvertise()
        scheduleStartAdvertise()
    }

    private val checkForFriendRunnable = Runnable {
        if (checkForFriends()) {
            isWithFriends = true
        }
        isWithFriends = true
        setState(isWithFriends,isAtWork)
        Thread.sleep(scanInterval)

        isWithFriends = false
        setState(isWithFriends,isAtWork)
        scheduleCheckForFriends()
    }

    private val checkForWorkplaceRunnable = Runnable {
    }

    private fun scheduleStartScan(){
        Log.d("bluetooth","scheduled")
        bluetoothHandler.postDelayed(startScanRunnable, scanInterval)
    }

    private fun scheduleStopScan(){
        bluetoothHandler.postDelayed(stopScanRunnable, scanInterval)
    }

    private fun scheduleStartAdvertise(){
        bluetoothHandler.postDelayed(startAdvertiseRunnable, scanInterval)
    }

    private fun scheduleStopAdvertise(){
        bluetoothHandler.postDelayed(stopAdvertiseRunnable, scanInterval)
    }

    private fun scheduleCheckForFriends() {
        friendHandler.postDelayed(checkForFriendRunnable,scanInterval)
    }

    private fun checkForFriends() : Boolean {
        val devices = bluetoothRepository.getDevices().value
        if (devices != null) {
            for (device in devices){
                return  UserRepository.user.value?.getFriendList()?.map{it.uuid}?.contains(device.data) == true
            }
        }
        return false
    }

    private fun checkWorkplace() : Boolean {
        return false
    }

    private fun setState(isWithFriends : Boolean, isAtWork : Boolean) {
        if (isAtWork){
            UserRepository.setState(State.WORKING)
        } else if (isWithFriends){
            UserRepository.setState(State.SOCIALLY_ENGAGED)
        } else {
            UserRepository.setState(State.IDLE)
        }
        userRepository.saveUser()

    }

    private fun startLocationService() {
        val locationRequest : LocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,1000).build()
        val locationListener : LocationListener = LocationListener { location ->
            Log.d("Loc",location.latitude.toString() + " " + location.longitude.toString())
            UserRepository.setLocation(location)
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationListener,
            locationThread.looper)
    }

}