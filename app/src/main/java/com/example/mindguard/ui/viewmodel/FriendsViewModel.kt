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
    private var _user : MutableLiveData<User> = MutableLiveData<User>()
    private val _uuid = MutableLiveData<String>()
    val uuid: LiveData<String> = _uuid
    private var _friendList = MutableLiveData<List<String>>()
    val friendList: LiveData<List<String>> = _friendList

    init {
        loadUserData()
        observeData()
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