package com.example.mindguard.logic

import java.util.UUID

class User(val name : String) {

    private var id : String =  UUID.randomUUID().toString()
    private var position : Int = 0
    private var state : State = State.IDLE
    private var friendList : MutableList<User> = mutableListOf()
    private var workplace : Int = 0


}