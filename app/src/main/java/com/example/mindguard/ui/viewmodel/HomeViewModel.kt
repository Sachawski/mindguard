package com.example.mindguard.ui.viewmodel

import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mindguard.data.model.User
import com.example.mindguard.data.repository.UserRepository
import com.example.mindguard.data.service.BackgroundService

class HomeViewModel(private val application: Application) : AndroidViewModel(application) {

    private val filePath = application.applicationContext.filesDir
    private val userRepository : UserRepository = UserRepository(filePath)
    private var _user : MutableLiveData<User> = MutableLiveData<User>()

    private val _showInputDialog = MutableLiveData<Boolean>()
    val showInputDialog: LiveData<Boolean> get() = _showInputDialog


    init {
        loadUserData()
        observeData()
    }

    // load user data from file if it exist, and initialize the usage stat
    private fun loadUserData(){
        if (!userRepository.userExists()) {
            showDialog()
        } else {
            val usageStats = userRepository.getUsageStats(application.applicationContext)
            _user = userRepository.loadUser() as MutableLiveData<User>
            _user.value!!.setUsageStats(usageStats)
            userRepository.saveUser(_user)

            // launch background service when user is loaded
            val serviceIntent = Intent(this.application, BackgroundService::class.java)
            this.application.startService(serviceIntent)
        }
    }

    // tell the ui to show the dialog for the user to type a name
    private fun showDialog() {
        _showInputDialog.value = true
    }

    // create User when the user first launch the app and type a name
    fun createUser(input: String) {
        _user = userRepository.initializeUser(input) as MutableLiveData<User>
        _showInputDialog.value = false
        loadUserData()
    }

    fun getUser() : LiveData<User>{
        return _user
    }

    private fun observeData(){
        UserRepository.user.observeForever { user ->
            try {
                _user.value = user
            } catch (exception : Exception) {
                Log.d("FriendsVM",exception.toString())
            }
        }
    }


    fun getUsageStats(): List<Pair<String, Long>> {
        if (_user.value != null) {
            return _user.value!!.getUsageStats()
        }
        return emptyList()
    }

    fun getTotalScreenTime(): Long {
        if (_user.value != null) {
            return _user.value!!.getTotalScreenTime()
        }
        return 0
    }

    // check if the app has access to usage data (for screen time)
    fun isAccessGranted(context: Context?): Boolean {
        val appOps = context?.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun formatMillisToHoursAndMinutes(millis: Long): String {
        val seconds = millis / 1000
        val minutes = (seconds / 60) % 60
        val hours = seconds / 3600
        return String.format("%02d:%02d:%02d", hours, minutes, seconds % 60)
    }


}


