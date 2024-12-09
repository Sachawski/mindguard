package com.example.mindguard.data.model

import android.util.Log
import kotlinx.serialization.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Serializable
class User(val name : String) {

    private var id : String =  UUID.randomUUID().toString()
    private var location : Pair<Double,Double> = Pair(0.0,0.0)
    private var state : State = State.IDLE
    private var _friendList : MutableList<Friend> = mutableListOf()
    private var workplace : MutableSet<Pair<Double,Double>> = mutableSetOf()
    private var usageStats : List<Pair<String,Long>> = listOf()

    // screenTimeInfos contains list of pair for each state the user can be in.
    // Those pairs contains a start time and a end time, allowing to compute the total screen time
    // for a given state
    private var todayTimeInfo : TimeInfo = TimeInfo()
    private var timeInfoHistory : MutableList<TimeInfo> = mutableListOf()
    private var todayAttentionScore : Double = 100.0
    private var attentionScoreHistory : MutableList<Double> = mutableListOf()

    private var screentimeHistoryUpdateDay : String
    private var screentimeHistoryCurrentDay : String
    private var attentionScoreHistoryUpdateDay : String
    private var attentionScoreHistoryCurrentDay : String

    init {
        val currentDate: LocalDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        screentimeHistoryUpdateDay = currentDate.format(formatter)
        screentimeHistoryCurrentDay = currentDate.format(formatter)
        attentionScoreHistoryUpdateDay = currentDate.format(formatter)
        attentionScoreHistoryCurrentDay = currentDate.format(formatter)
    }

    fun getId() : String{
        return id
    }

    fun getLocation() : Pair<Double,Double>{
        return Pair(location.first,location.second)
    }

    fun getState() : State {
        return state
    }

    fun getFriendList() : List<Friend> {
        val immutableFriendList : List<Friend> = _friendList
        return immutableFriendList
    }

    fun getWorkplace() : MutableSet<Pair<Double,Double>>{
        return workplace
    }

    fun getUsageStats() : List<Pair<String,Long>> {
        return usageStats
    }

    fun getScreenTimeInfos() : TimeInfo{
        return todayTimeInfo
    }

    fun addScreenTimeInfo(timePair : Pair<Long,Long>, state : State){
        when (state){
            State.SOCIALLY_ENGAGED -> {
                todayTimeInfo.addSocialScreenTime(timePair)
            }
            State.WORKING ->{
                todayTimeInfo.addWorkScreenTime(timePair)
            }
            else -> {}
        }
    }

    fun addTimeInfo(timePair : Pair<Long,Long>, state : State){
        when (state){
            State.SOCIALLY_ENGAGED -> {
                todayTimeInfo.addSocialTime(timePair)
            }
            State.WORKING -> {
                todayTimeInfo.addWorkTime(timePair)
            }
            else -> {}
        }
    }

    fun getAttentionScore() : Double{
        val timeInfo : TimeInfo = getScreenTimeInfos()
        val screenTimeAtWork = timeInfo.getTotalWorkScreenTime()
        val timeAtWork = timeInfo.getTotalWorkTime()
        val screenTimeWithFriends = timeInfo.getTotalSocialScreenTime()
        val timeWithFriends = timeInfo.getTotalSocialTime()
        return (((timeAtWork + timeWithFriends) - ( screenTimeAtWork + screenTimeWithFriends)) /
                (timeAtWork + timeWithFriends)).toDouble()
    }


    fun screenTimeDailyUpdate(){
        if (screentimeHistoryCurrentDay == screentimeHistoryUpdateDay) {
            timeInfoHistory[timeInfoHistory.size-1] = todayTimeInfo
        } else {
            timeInfoHistory.add(todayTimeInfo)
            todayTimeInfo = TimeInfo()
        }
    }

    fun attentionScoreDailyUpdate(){
        if (attentionScoreHistoryCurrentDay == attentionScoreHistoryUpdateDay) {
            attentionScoreHistory[attentionScoreHistory.size-1] = todayAttentionScore
        } else {
            attentionScoreHistory.add(todayAttentionScore)
            todayAttentionScore = 100.0
        }
    }

    fun setState(newState : State){
        state = newState
    }

    fun setLocation(latitude : Double, longitude : Double){
        location = Pair(latitude,longitude)
    }

    fun addWorkplace(latitude : Double, longitude : Double){
        workplace.add(Pair(latitude,longitude))
    }

    fun removeWorkplace(latitude : Double, longitude : Double){
        workplace.remove(Pair(latitude,longitude))
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