package com.example.mindguard.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.serialization.json.*
import com.example.mindguard.data.model.User
import java.io.File

class UserRepository(private val filesDir : File) {

    private val filePath : File = File(filesDir, "user.json")

    fun userExists() : Boolean {
        return filePath.exists()
    }

    fun getUser() : LiveData<User> {
        val userLiveData = MutableLiveData<User>()
        val user = loadUserFile()
        userLiveData.value = user
        return userLiveData
    }

    fun initializeUser(name : String) : LiveData<User> {
        val newUser = User(name)
        val jsonString = Json.encodeToString(User.serializer(), newUser)
        Log.i("serialized : ", jsonString)
        Log.d("path : ",filePath.toString())
        filePath.writeText(jsonString)
        val userLiveData = MutableLiveData<User>()
        userLiveData.value = newUser
        return userLiveData

    }

    fun saveUser(userLiveData : MutableLiveData<User>){
        val user = userLiveData.value
        if (user != null) {
            val jsonString = Json.encodeToString(User.serializer(), user)
            Log.i("serialized : ", jsonString)
            filePath.writeText(jsonString)
        }
    }

    private fun loadUserFile() : User {
        val serializedUser = filePath.readText()
        val user = Json.decodeFromString<User>(serializedUser)
        Log.i("deserialized : ", user.toString())
        return user
    }
}