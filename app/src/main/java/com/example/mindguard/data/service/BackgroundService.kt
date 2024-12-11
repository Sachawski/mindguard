package com.example.mindguard.data.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.mindguard.R
import com.example.mindguard.data.model.State
import com.example.mindguard.data.model.User
import com.example.mindguard.data.repository.BluetoothRepository
import com.example.mindguard.data.repository.UserRepository
import com.example.mindguard.ui.activity.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class BackgroundService() : Service()  {

    //To save the user periodically, I've arbitrary chosen the screenTimeThread to do it.

    private var filePath : File = File("")
    private lateinit var userRepository : UserRepository
    private lateinit var bluetoothRepository : BluetoothRepository
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var notificationManager: NotificationManager

    private lateinit var friendThread : HandlerThread
    private lateinit var friendHandler : Handler

    private lateinit var workplaceThread : HandlerThread
    private lateinit var workplaceHandler : Handler

    private lateinit var bluetoothThread : HandlerThread
    private lateinit var bluetoothHandler : Handler

    private lateinit var screenTimeThread : HandlerThread
    private lateinit var screenTimeHandler : Handler

    private lateinit var locationThread : HandlerThread
    private lateinit var locationHandler : Handler

    private var recordingFriendTime = false
    private var recordingFriendScreenTime = false
    private var recordingWorkTime = false
    private var recordingWorkScreenTime = false

    private var friendScreenStartTime : Long = System.currentTimeMillis()
    private var friendScreenEndTime : Long = System.currentTimeMillis()
    private var friendStartTime : Long = System.currentTimeMillis()
    private var friendEndTime : Long = System.currentTimeMillis()

    private var workScreenStartTime : Long = System.currentTimeMillis()
    private var workScreenEndTime : Long = System.currentTimeMillis()
    private var workStartTime : Long = System.currentTimeMillis()
    private var workEndTime : Long = System.currentTimeMillis()

    private var isAtWork = false
    private var isWithFriends = false
    private var isScreenOn = true

    private val scanInterval: Long = 10000
    private val workplaceRadius : Double = 20.0
    private var channel_id : String = ""
    private val locationListener : LocationListener = LocationListener { location ->
        UserRepository.setLocation(location)
    }

    override fun onCreate() {
        super.onCreate()
        filePath = applicationContext.filesDir
        userRepository = UserRepository(filePath)
        bluetoothRepository = BluetoothRepository(applicationContext)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationManager = this.getSystemService(NotificationManager::class.java)
        friendThread = HandlerThread("Friend Thread")
        friendThread.start()
        friendHandler = Handler(friendThread.looper)

        workplaceThread = HandlerThread("Workplace Thread")
        workplaceThread.start()
        workplaceHandler = Handler(workplaceThread.looper)

        bluetoothThread = HandlerThread("Bluetooth Thread")
        bluetoothThread.start()
        bluetoothHandler = Handler(bluetoothThread.looper)

        screenTimeThread = HandlerThread("Screen time Thread")
        screenTimeThread.start()
        screenTimeHandler = Handler(screenTimeThread.looper)

        locationThread = HandlerThread("location Thread")
        locationThread.start()
        locationHandler = Handler(locationThread.looper)

        channel_id = createNotificationChannel()
        startLocationService(locationListener)
        scheduleSaveAttentionScore()
        scheduleStartScan()
        scheduleStartAdvertise()
        scheduleCheckForFriends()
        scheduleCheckForWorkplace()
        launchingScreenTimeRecorder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        // Do your task here (e.g., location tracking, file download)
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "foreground_service_channel"
        val channelName = "ForegroundService"

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Foreground Service")
            .setContentText("This service is running in the foreground.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()

        // Start the service in the foreground
        startForeground(1, notification)
    }


    override fun onDestroy() {
        super.onDestroy()
        userRepository.saveUser()
        fusedLocationClient.removeLocationUpdates(locationListener)
        bluetoothHandler.removeCallbacksAndMessages(null)
        screenTimeHandler.removeCallbacksAndMessages(null)
        friendHandler.removeCallbacksAndMessages(null)
        locationHandler.removeCallbacksAndMessages(null)
        workplaceHandler.removeCallbacksAndMessages(null)
        workplaceThread.quitSafely()
        bluetoothThread.quitSafely()
        screenTimeThread.quitSafely()
        friendThread.quitSafely()
        locationThread.quitSafely()
    }

    override fun onBind(intent: Intent?): IBinder? {
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
                            isScreenOn = true
                            if (recordingWorkTime) {
                                workScreenStartTime = System.currentTimeMillis()
                            }
                            if (recordingFriendTime) {
                                friendScreenStartTime = System.currentTimeMillis()
                            }
                            if (UserRepository.user.value!!.getState() != State.IDLE){
                                sendNotifications(UserRepository.user.value!!.getState())
                            } else {
                                removeNotifications()
                            }
                        }
                        Intent.ACTION_SCREEN_OFF -> {
                            isScreenOn = false
                            if (recordingWorkTime) {
                                workScreenEndTime = System.currentTimeMillis()
                                val timePair = Pair(workScreenStartTime, workScreenEndTime)
                                UserRepository.user.value!!.addScreenTimeInfo(
                                    timePair,
                                    State.WORKING
                                )
                                recordingWorkScreenTime = false
                            }
                            if (recordingFriendTime) {
                                friendScreenEndTime = System.currentTimeMillis()
                                val timePair = Pair(friendScreenStartTime, friendScreenEndTime)
                                UserRepository.user.value!!.addScreenTimeInfo(
                                    timePair,
                                    State.SOCIALLY_ENGAGED
                                )
                                recordingFriendScreenTime = false
                            }

                        }
                    }
                    Log.d("screen time",UserRepository.user.value!!.getScreenTimeInfos().toString())
                }
            }
            application.registerReceiver(screenStateReceiver, filter)
        }
    }

    private val startScanRunnable = Runnable {
        bluetoothRepository.startScan()
        scheduleStopScan()
    }

    private val stopScanRunnable = Runnable {
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

    private val saveAttentionScoreAndResetScreenTimeRunnable = Runnable {
        UserRepository.setAttentionScore()
        UserRepository.saveAttentionScoreAndResetScreenTime()
        scheduleSaveAttentionScore()
    }

    private val checkForWorkplaceRunnable = Runnable    {

        if (checkForWorkplace()) {
            isAtWork = true
            if (isScreenOn && !recordingWorkScreenTime) {
                //if the screen is turned on, but the screen time recording has not been started
                recordingWorkScreenTime = true
                workScreenStartTime = System.currentTimeMillis()
            }

            if (!recordingWorkTime){
                workStartTime = System.currentTimeMillis()
                recordingWorkTime = true
            } else {
                workEndTime = System.currentTimeMillis()
                val timePair = Pair(workStartTime, workEndTime)
                UserRepository.addTimeInfo(
                    timePair,
                    State.WORKING
                )
                workStartTime = System.currentTimeMillis()
            }
            if (recordingWorkScreenTime){
                workScreenEndTime = System.currentTimeMillis()
                val timePair = Pair(workScreenStartTime, workScreenEndTime)
                UserRepository.addScreenTimeInfo(
                    timePair,
                    State.WORKING
                )
                workScreenStartTime = System.currentTimeMillis()
            }
        } else {
            isAtWork = false
            if (recordingWorkTime){
                workEndTime = System.currentTimeMillis()
                val timePair = Pair(workStartTime, workEndTime)
                UserRepository.addTimeInfo(
                    timePair,
                    State.WORKING
                )
            }
            if (recordingWorkScreenTime){
                workScreenEndTime = System.currentTimeMillis()
                val timePair = Pair(workScreenStartTime, workScreenEndTime)
                UserRepository.addScreenTimeInfo(
                    timePair,
                    State.WORKING
                )
            }
            recordingWorkTime = false
            recordingWorkScreenTime = false
        }
        Log.d("recordingWorkScreenTime ", recordingWorkScreenTime.toString())
        Log.d("recordingWorkTime ", recordingWorkTime.toString())
        Log.d("workWithoutScreenStartTime ", workStartTime.toString())
        Log.d("workWithoutScreenEndTime ", workEndTime.toString())
        Log.d("screen time",UserRepository.user.value!!.getScreenTimeInfos().toString())
        setState()
        scheduleCheckForWorkplace()
    }

    private val checkForFriendRunnable = Runnable {
        // We prioritize the work over the friends here
        if (UserRepository.user.value!!.getState() != State.WORKING) {
            if (checkForFriends()) {
                isWithFriends = true
                if (isScreenOn && !recordingFriendScreenTime) {
                    //if the screen is turned on, but the screen time recording has not been started
                    recordingFriendScreenTime = true
                    friendScreenStartTime = System.currentTimeMillis()
                }

                if (!recordingFriendTime){
                    friendStartTime = System.currentTimeMillis()
                    recordingWorkTime = true
                } else {
                    friendEndTime = System.currentTimeMillis()
                    val timePair = Pair(friendStartTime, friendEndTime)
                    UserRepository.addTimeInfo(
                        timePair,
                        State.SOCIALLY_ENGAGED
                    )
                    friendStartTime = System.currentTimeMillis()
                }
                if (recordingFriendScreenTime){
                    friendScreenEndTime = System.currentTimeMillis()
                    val timePair = Pair(friendScreenStartTime, friendScreenEndTime)
                    UserRepository.addScreenTimeInfo(
                        timePair,
                        State.SOCIALLY_ENGAGED
                    )
                    friendScreenStartTime = System.currentTimeMillis()
                }
            } else {
                isWithFriends = false
                if (recordingFriendTime){
                    friendEndTime = System.currentTimeMillis()
                    val timePair = Pair(friendStartTime, friendEndTime)
                    UserRepository.addTimeInfo(
                        timePair,
                        State.SOCIALLY_ENGAGED
                    )
                }
                if (recordingFriendScreenTime){
                    friendScreenEndTime = System.currentTimeMillis()
                    val timePair = Pair(friendScreenStartTime, friendScreenEndTime)
                    UserRepository.addScreenTimeInfo(
                        timePair,
                        State.SOCIALLY_ENGAGED
                    )
                }
                recordingFriendTime = false
                recordingFriendScreenTime = false
            }
            Log.d("recordingFriendTime ", recordingFriendTime.toString())
            Log.d("recordingFriendScreenTime ", recordingFriendScreenTime.toString())
            Log.d("friendStartTime ", friendStartTime.toString())
            Log.d("friendEndTime ", friendEndTime.toString())
            Log.d("screen time",UserRepository.user.value!!.getScreenTimeInfos().toString())

        }
        scheduleCheckForFriends()
    }

    private fun scheduleSaveAttentionScore(){
        userRepository.saveUser()
        screenTimeHandler.postDelayed(saveAttentionScoreAndResetScreenTimeRunnable,scanInterval)
    }

    private fun scheduleStartScan(){
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
        Log.d("CheckForFriends","Ping")
        friendHandler.postDelayed(checkForFriendRunnable,scanInterval)
    }

    private fun scheduleCheckForWorkplace() {
        workplaceHandler.postDelayed(checkForWorkplaceRunnable,scanInterval)
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

    private fun checkForWorkplace() : Boolean {
        val workplaces = UserRepository.user.value?.getWorkplace()
        val userLocation = UserRepository.user.value!!.getLocation()
        if (workplaces != null) {
            for (workplace in workplaces) {
                val distance = latLonToMeters(workplace.first,workplace.second,userLocation.first,userLocation.second)
                Log.d("distance to workplace",distance.toString())
                if (distance < workplaceRadius ){
                    return true
                }
            }
        }
        return false
    }

    private fun startLocationService(locationListener : LocationListener) {
        val locationRequest : LocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,1000).build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationListener,
            locationThread.looper)
    }

    private fun latLonToMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radius = 6371000.0

        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        val dLat = lat2Rad - lat1Rad
        val dLon = lon2Rad - lon1Rad

        val a = sin(dLat / 2).pow(2.0) +
                cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return radius * c
    }

    private fun setState(){
        if (isAtWork) {
            UserRepository.setState(State.WORKING)
        } else if (isWithFriends) {
            UserRepository.setState(State.SOCIALLY_ENGAGED)
        } else {
            UserRepository.setState(State.IDLE)
        }
    }

    private fun createNotificationChannel() : String {
        channel_id = "reminder_channel"
        val channel = NotificationChannel(channel_id, "Reminder Notification Channel", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
        return channel_id
    }

    private fun sendNotifications(state : State){
        if (state == State.SOCIALLY_ENGAGED) {
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(this, channel_id)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("MindGuard reminder")
                .setContentText("You are with your friends, maybe turn your phone off !")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()
            notificationManager.notify(1,notification)
        } else {
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(this, channel_id)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("MindGuard reminder")
                .setContentText("You are at work, maybe turn your phone off !")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()
            notificationManager.notify(1,notification)
        }
    }

    private fun removeNotifications(){
        notificationManager.cancelAll()
    }

}