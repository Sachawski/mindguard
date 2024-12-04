package com.example.mindguard.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mindguard.data.model.Friend
import com.example.mindguard.data.model.User
import com.example.mindguard.data.repository.UserRepository

class FriendsViewModel(private val application: Application) : AndroidViewModel(application) {

    private val filePath = application.applicationContext.filesDir
    private val userRepository : UserRepository = UserRepository(filePath)

    private var _user : MutableLiveData<User> = MutableLiveData<User>()

    init {
        observeData()
    }

    private fun observeData(){
        UserRepository.user.observeForever { user ->
            try {
                _user.value = user
                Log.d("user changed","in repo")
            } catch (exception : Exception) {
                Log.d("FriendsVM",exception.toString())
            }
        }
    }

    fun getUser() : LiveData<User>{
        return _user
    }

    fun addFriendToUser(name : String, uuid : String){
        val friend = Friend(name,uuid)
        UserRepository.addFriend(friend)
        userRepository.saveUser()
    }

    fun removeFriendFromUser(friend:Friend){
        UserRepository.deleteFriend(friend)
        userRepository.saveUser()
    }

}