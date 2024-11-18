package com.example.mindguard.data.model

import kotlinx.serialization.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Serializable
class User(val name : String) {

    private var id : String =  UUID.randomUUID().toString()
    private var position : Int = 0
    private var state : State = State.IDLE
    private var _friendList : MutableList<String> = mutableListOf()
    private var workplace : Int = 0
    private var usageStats : List<Pair<String,Long>> = listOf()
    private var updateDay : String

    init {
        val currentDate: LocalDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        updateDay = currentDate.format(formatter)
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

    fun getUsageStats() : List<Pair<String,Long>> {
        return usageStats
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

    fun setUsageStats(newUsageStats : List<Pair<String,Long>>){
        usageStats = newUsageStats
    }

    fun getTotalScreenTime() : Long {
        var totalScreenTime : Long = 0
        for (pair in usageStats){
            totalScreenTime += pair.second
        }
        return totalScreenTime
    }

    fun addFriend(uuid : String){
        if (!_friendList.contains(uuid)) {
            _friendList.add(uuid)
        }
    }

    fun deleteFriends(uuid : String){
        _friendList.remove(uuid)
    }


}