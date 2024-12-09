package com.example.mindguard.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mindguard.BuildConfig
import com.example.mindguard.data.model.Friend
import com.example.mindguard.data.model.User
import com.example.mindguard.data.repository.UserRepository


class WorkplaceViewModel(application: Application) : AndroidViewModel(application) {

    private val filePath = application.applicationContext.filesDir
    private val userRepository : UserRepository = UserRepository(filePath)
    private var _user : MutableLiveData<User> = MutableLiveData<User>()

    private val _text = MutableLiveData<String>().apply {
        value = "Please pin your work location"
    }
    val text: LiveData<String> = _text

    init {
        observeData()
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

    fun addWorkplaceToUser(latitude : Double, longitude : Double){
        UserRepository.addWorkplace(latitude,longitude)
        userRepository.saveUser()
    }

    fun removeWorkplaceToUser(latitude : Double, longitude : Double){
        UserRepository.removeWorkplace(latitude,longitude)
        userRepository.saveUser()
    }

}