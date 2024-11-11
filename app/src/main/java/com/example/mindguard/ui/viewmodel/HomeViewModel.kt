package com.example.mindguard.ui.viewmodel

import android.app.AlertDialog
import android.app.Application
import android.util.Log
import android.widget.EditText
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mindguard.data.model.User
import com.example.mindguard.data.repository.UserRepository

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val filePath = application.applicationContext.filesDir
    private val userRepository : UserRepository = UserRepository(filePath)

    private val _showInputDialog = MutableLiveData<Boolean>()
    val showInputDialog: LiveData<Boolean> get() = _showInputDialog
    private val _userInput = MutableLiveData<String>()
    private val _text = MutableLiveData<String>().apply {value = "This is home Fragment"}
    val text: LiveData<String> = _text

    init {
        loadUser()
    }

    private fun loadUser(){
        if (!userRepository.userExists()){
            showDialog()
        }
    }

    private fun showDialog() {
        _showInputDialog.value = true
    }

    fun setUserInput(input: String) {
        val user = userRepository.initializeUser(input)
    }



}


