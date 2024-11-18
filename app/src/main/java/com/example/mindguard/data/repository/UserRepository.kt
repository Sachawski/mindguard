package com.example.mindguard.data.repository

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.serialization.json.*
import com.example.mindguard.data.model.User
import java.io.File

class UserRepository(private val filesDir : File) {

    private val filePath: File = File(filesDir, "user.json")

    private fun loadUserFile(): User {
        val serializedUser = filePath.readText()
        val user = Json.decodeFromString<User>(serializedUser)
        Log.i("deserialized : ", user.toString())
        return user
    }

    fun userExists(): Boolean {
        return filePath.exists()
    }

    fun getUser(): LiveData<User> {
        val userLiveData = MutableLiveData<User>()
        val user = loadUserFile()
        userLiveData.value = user
        return userLiveData
    }

    fun initializeUser(name: String): LiveData<User> {
        val newUser = User(name)
        val jsonString = Json.encodeToString(User.serializer(), newUser)
        Log.i("serialized : ", jsonString)
        Log.d("path : ", filePath.toString())
        filePath.writeText(jsonString)
        val userLiveData = MutableLiveData<User>()
        userLiveData.value = newUser
        return userLiveData

    }

    fun saveUser(userLiveData: LiveData<User>) {
        val user = userLiveData.value
        if (user != null) {
            val jsonString = Json.encodeToString(User.serializer(), user)
            Log.i("serialized : ", jsonString)
            filePath.writeText(jsonString)
            Log.d("saving", jsonString)
        }
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