package com.example.mindguard.data.model

import android.util.Log
import kotlinx.serialization.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Serializable
class User(val name : String) {

    private var id : String =  UUID.randomUUID().toString()
    private var position : Int = 0
    private var state : State = State.IDLE
    private var _friendList : MutableList<Friend> = mutableListOf()
    private var workplace : Int = 0
    private var usageStats : List<Pair<String,Long>> = listOf()

    // screenTimeInfos contains list of pair for each state the user can be in.
    // Those pairs contains a start time and a end time, allowing to compute the total screen time
    // for a given state
    private var todayTimeInfo : TimeInfo = TimeInfo()
    private var timeInfoHistory : MutableList<TimeInfo> = mutableListOf()
    private var todayAttentionScore : Double = 100.0
    private var AttentionScoreHistory : MutableList<Double> = mutableListOf()

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

    fun getFriendList() : List<Friend> {
        val immutableFriendList : List<Friend> = _friendList
        return immutableFriendList
    }

    fun getWorkplace() : Int{
        return workplace
    }

    fun getUsageStats() : List<Pair<String,Long>> {
        return usageStats
    }



    fun getScreenTimeInfos() : TimeInfo{
        return todayTimeInfo
    }

    fun addScreenTime(timePair : Pair<Long,Long>, state : State){
        when (state){
            State.IDLE -> todayTimeInfo.addIdleScreenTime(timePair)
            State.SOCIALLY_ENGAGED -> todayTimeInfo.addSocialScreenTime(timePair)
            State.WORKING -> todayTimeInfo.addWorkScreenTime(timePair)
        }
    }

    fun getAttentionScore() {

    }


    fun screenTimeDailyUpdate(){
        timeInfoHistory.add(todayTimeInfo)
        todayTimeInfo = TimeInfo()
    }

    fun attentionScoreDailyUpdate(){
        timeInfoHistory.add(todayTimeInfo)
        todayTimeInfo = TimeInfo()
    }

    fun setState(newState : State){
        state = newState
        Log.d("changed state to",newState.toString())
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

    fun addFriend(friend : Friend){
        if (!_friendList.contains(friend)) {
            _friendList.add(friend)
        }
    }

    fun deleteFriend(friend : Friend){
        _friendList.remove(friend)
    }


}