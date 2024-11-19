package com.example.mindguard.ui.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mindguard.data.model.User
import com.example.mindguard.data.repository.BluetoothRepository
import com.example.mindguard.data.repository.UserRepository

class FriendsViewModel(private val application: Application) : AndroidViewModel(application) {

    private val filePath = application.applicationContext.filesDir
    private val userRepository : UserRepository = UserRepository(filePath)
    private val bluetoothRepository : BluetoothRepository = BluetoothRepository(application.applicationContext)

    private var _user : MutableLiveData<User> = MutableLiveData<User>()

    private val _uuid = MutableLiveData<String>()
    val uuid: LiveData<String> = _uuid

    private var _friendList = MutableLiveData<List<String>>()
    val friendList: LiveData<List<String>> = _friendList

    private val scanInterval: Long = 10000
    //private val pauseInterval: Long = 120000

    private val handler = Handler(Looper.getMainLooper())

    private val startScanRunnable = Runnable {
        bluetoothRepository.startScan()
        scheduleStopScan()
    }

    private val stopScanRunnable = Runnable {
        bluetoothRepository.stopScan()
        scheduleStartScan()
    }

    private val startAdvertiseRunnable = Runnable {
        _uuid.value?.let { bluetoothRepository.startAdvertise(it) }
        scheduleStopAdvertise()
    }

    private val stopAdvertiseRunnable = Runnable {
        bluetoothRepository.stopAdvertise()
        scheduleStartAdvertise()
    }

    init {
        loadUserData()
        observeData()
        scheduleStartScan()
        scheduleStartAdvertise()
    }

    private fun scheduleStartScan(){
        Log.i("Bluetooth devices" , bluetoothRepository.getDevices().value.toString())
        handler.postDelayed(startScanRunnable, scanInterval)
    }

    private fun scheduleStopScan(){
        Log.i("Bluetooth devices" , bluetoothRepository.getDevices().value.toString())
        handler.postDelayed(stopScanRunnable, scanInterval)
    }

    private fun scheduleStartAdvertise(){
        Log.i("adv","Schedule start in VM")
        handler.postDelayed(startAdvertiseRunnable, scanInterval)
    }

    private fun scheduleStopAdvertise(){
            Log.i("adv" ,"Schedule stop in VM")
            handler.postDelayed(stopAdvertiseRunnable, scanInterval)
    }

    private fun loadUserData() {
        _user = userRepository.getUser() as MutableLiveData<User>
    }

    private fun observeData(){
        _user.observeForever { user ->
            try {
                _friendList.value =
                    user.getFriendList()
                _uuid.value =
                    user.getId()
            } catch (exception : Exception) {
                Log.d("debugging",exception.toString())
            }
        }
    }

    fun addFriendToUser(uuid : String){
        _user.value?.let {
            it.addFriend(uuid)
            _friendList.value = it.getFriendList()
            userRepository.saveUser(_user)
        }
    }


}