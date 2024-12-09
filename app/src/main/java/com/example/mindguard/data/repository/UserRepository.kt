package com.example.mindguard.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mindguard.data.model.Friend
import com.example.mindguard.data.model.State
import kotlinx.serialization.json.*
import com.example.mindguard.data.model.User
import java.io.File

class UserRepository(private val filesDir : File) {

    private val filePath: File = File(filesDir, "user.json")

    companion object {
        private var _user : MutableLiveData<User> = MutableLiveData<User>()
        var user: MutableLiveData<User> = _user

        fun setLocation(location : Location){
            user.value?.setLocation(location.latitude,location.longitude)
            user.postValue(user.value)
        }

        fun setState(state : State){
            user.value?.setState(state)
            user.postValue(user.value)
        }

        fun addFriend(friend : Friend){
            user.value?.addFriend(friend)
            user.postValue(user.value)
        }

        fun deleteFriend(friend : Friend){
            user.value?.deleteFriend(friend)
            user.postValue(user.value)
        }

        fun addTimeInfo(timePair : Pair<Long,Long>, state : State){
            user.value?.addTimeInfo(timePair,state)
            user.postValue(user.value)
        }

        fun addWorkplace(latitude : Double, longitude : Double){
            user.value?.addWorkplace(latitude,longitude)
            user.postValue(user.value)
        }

        fun removeWorkplace(latitude : Double, longitude : Double){
            user.value?.removeWorkplace(latitude,longitude)
            user.postValue(user.value)
        }
    }

    private fun loadUserFile(): User {
        synchronized(this) {
            val serializedUser = filePath.readText()
            val user = Json.decodeFromString<User>(serializedUser)
            return user
        }
    }

    fun initializeUser(name: String): LiveData<User> {
        val newUser = User(name)
        val jsonString = Json.encodeToString(User.serializer(), newUser)
        filePath.writeText(jsonString)
        val userLiveData = MutableLiveData<User>()
        userLiveData.value = newUser
        return userLiveData
    }

    fun loadUser(): LiveData<User> {
        val userLiveData = MutableLiveData<User>()
        val user = loadUserFile()
        userLiveData.value = user
        _user.postValue(user)
        return userLiveData
    }

    // save the specified user in the file (used by view model)
    fun saveUser(userLiveData: LiveData<User>) {
        val user = userLiveData.value
        if (user != null) {
            synchronized(this) {
                val jsonString = Json.encodeToString(User.serializer(), user)
                filePath.writeText(jsonString)
                _user.postValue(userLiveData.value)
                Log.i("SAVE", "user has been saved : $jsonString")
            }
        }
    }

    //save the user in the companion object in the file
    fun saveUser() {
        synchronized(this) {
            val jsonString = Json.encodeToString(User.serializer(), _user.value!!)
            filePath.writeText(jsonString)
            Log.i("SAVE", "user has been saved : $jsonString")
        }

    }

    fun userExists(): Boolean {
        return filePath.exists()
    }

    fun getUsageStats(context: Context): List<Pair<String, Long>> {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 24 * 60 * 60 * 1000
        val stats =
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        val usageStats: MutableList<Pair<String, Long>> = mutableListOf()
        if (stats != null && stats.isNotEmpty()) {
            for (usageStat in stats) {
                val packageName = usageStat.packageName.substring(usageStat.packageName.lastIndexOf('.') +1 )
                val totalTimeInForeground = usageStat.totalTimeInForeground
                if (totalTimeInForeground != 0.toLong()) {
                    usageStats.add(Pair(packageName, totalTimeInForeground))
                }
            }
        }
        return usageStats.sortedByDescending { it.second }
    }


}