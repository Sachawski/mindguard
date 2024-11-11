package com.example.mindguard.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mindguard.data.model.User
import com.example.mindguard.data.repository.UserRepository

class FriendsViewModel(application: Application) : AndroidViewModel(application) {

    private val filePath = application.applicationContext.filesDir
    private val userRepository : UserRepository = UserRepository(filePath)

    private val _text = MutableLiveData<String>().apply {
        value = "Friend list :"
    }
    val text: LiveData<String> = _text
    private val _friendList = MutableLiveData<List<String>>()
    val friendList: LiveData<List<String>> = _friendList

    init {
        loadUserData()
    }

    private fun loadUserData() {
        Log.d("debugging","loading data to the view model")
        userRepository.getUser().observeForever { user ->
            try {
                _friendList.value =
                    user.getFriendList()
            } catch (exception : Exception) {
                Log.d("debugging",exception.toString())
            }
    }
        Log.d("debugging","observing")
    }
}