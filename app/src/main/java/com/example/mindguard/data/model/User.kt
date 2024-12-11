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
    private var todayAttentionScore : Double = 100.0
    private var attentionScoreHistory : HashMap<String,Double> = hashMapOf()

    private var attentionScoreHistoryUpdateDay : String

    init {
        val currentDate: LocalDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        attentionScoreHistoryUpdateDay = currentDate.format(formatter)
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

    fun setAttentionScore(){
        val timeInfo: TimeInfo = getScreenTimeInfos()
        val screenTimeAtWork = timeInfo.getTotalWorkScreenTime().toDouble()
        val timeAtWork = timeInfo.getTotalWorkTime().toDouble()
        val screenTimeWithFriends = timeInfo.getTotalSocialScreenTime().toDouble()
        val timeWithFriends = timeInfo.getTotalSocialTime().toDouble()
        if (timeAtWork + timeWithFriends > 0) {
            if ((((timeAtWork + timeWithFriends) - (screenTimeAtWork + screenTimeWithFriends)) / (timeAtWork + timeWithFriends)) * 100 < 0){
                todayAttentionScore = 0.0
            } else {
                todayAttentionScore = (((timeAtWork + timeWithFriends) - (screenTimeAtWork + screenTimeWithFriends)) / (timeAtWork + timeWithFriends)) * 100
            }
        }else {
            todayAttentionScore = 100.0
        }
    }

    fun getAttentionScore() : Double {
        return todayAttentionScore

    }

    fun attentionScoreUpdate(){
        val currentDate: LocalDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val currentDay = currentDate.format(formatter)
        Log.d("update day ",attentionScoreHistoryUpdateDay)
        Log.d("current day",currentDay)

        if (currentDay == attentionScoreHistoryUpdateDay) {
            attentionScoreHistory[currentDay] = todayAttentionScore
        } else {
            attentionScoreHistory[currentDay] = todayAttentionScore
            attentionScoreHistoryUpdateDay = currentDay
            todayAttentionScore = 100.0
            todayTimeInfo = TimeInfo()
        }
        Log.d("todayAttentionScore",todayAttentionScore.toString())
        Log.d("history",attentionScoreHistory.toString())

    }

    fun getAttentionScoreHistory() : HashMap<String,Double> {
        return attentionScoreHistory
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