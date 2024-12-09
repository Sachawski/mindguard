package com.example.mindguard.data.model

import kotlinx.serialization.*

@Serializable
class TimeInfo {
    // THIS FILE IS COMMITED BUT NEED ADJUSTMENT (changing variable names, because im going to change the logic a bit)
    private val workScreenTime: MutableList<Pair<Long, Long>> = mutableListOf()
    private val socialScreenTime: MutableList<Pair<Long, Long>> = mutableListOf()
    private val workTime: MutableList<Pair<Long, Long>> = mutableListOf()
    private val socialTime: MutableList<Pair<Long, Long>> = mutableListOf()

    fun getTotalWorkScreenTime() : Long {
        var totalTime = 0L
        for (pair in workScreenTime) {
            totalTime += (pair.second - pair.first)
        }
        return totalTime
    }

    fun getTotalWorkTime() : Long {
        var totalTime = 0L
        for (pair in workTime) {
            totalTime += (pair.second - pair.first)
        }
        return totalTime
    }

    fun getTotalSocialScreenTime() : Long {
        var totalTime = 0L
        for (pair in socialScreenTime) {
            totalTime += (pair.second - pair.first)
        }
        return totalTime
    }

    fun getTotalSocialTime() : Long {
        var totalTime = 0L
        for (pair in socialTime) {
            totalTime += (pair.second - pair.first)
        }
        return totalTime
    }

    fun addWorkScreenTime(pair : Pair<Long,Long>) {
        workScreenTime.add(pair)
    }

    fun addWorkTime(pair : Pair<Long,Long>) {
        workTime.add(pair)
    }

    fun addSocialScreenTime(pair : Pair<Long,Long>) {
        socialScreenTime.add(pair)
    }

    fun addSocialTime(pair : Pair<Long,Long>) {
        socialTime.add(pair)
    }

    override fun toString() : String{
        return " Social screen time : " + (getTotalSocialScreenTime()/1000).toString() +
                " // Work screen time: " + (getTotalWorkScreenTime()/1000).toString() +
                " // Social time : " + (getTotalSocialTime()/1000).toString() +
                " // Time at work: " + (getTotalWorkTime()/1000).toString()
    }

}