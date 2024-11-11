package com.example.mindguard.data.model

import kotlinx.serialization.*
import java.util.UUID

@Serializable
class User(val name : String) {

    private var id : String =  UUID.randomUUID().toString()
    private var position : Int = 0
    private var state : State = State.IDLE
    private var _friendList : MutableList<String> = mutableListOf()
    private var workplace : Int = 0

    fun addFriend(uuid : String){
        if (!_friendList.contains(uuid)) {
            _friendList.add(uuid)
        }
    }

    fun deleteFriends(uuid : String){
        _friendList.remove(uuid)
    }

    fun getId() : String{
        return id
    }

    fun getPosition() : Int{
        return position
    }

    fun getState() : State {
        return state
    }

    fun getFriendList() : List<String> {
        val immutableFriendList : List<String> = _friendList
        return immutableFriendList
    }

    fun getWorkplace() : Int{
        return workplace
    }

    fun setPosition(newPosition : Int){
        position = newPosition
    }

    fun setState(newPosition : Int){
        position = newPosition
    }

    fun setWorkplace(newWorkplace : Int){
        workplace = newWorkplace
    }

}