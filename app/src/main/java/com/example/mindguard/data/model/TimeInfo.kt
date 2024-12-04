package com.example.mindguard.data.model

import kotlinx.serialization.*

@Serializable
class TimeInfo {
    // THIS FILE IS COMMITED BUT NEED ADJUSTMENT (changing variable names, because im going to change the logic a bit)
    private val idleScreenTime: MutableList<Pair<Long, Long>> = mutableListOf()
    private val workScreenTime: MutableList<Pair<Long, Long>> = mutableListOf()
    private val socialScreenTime: MutableList<Pair<Long, Long>> = mutableListOf()
    private val idleWithoutScreenTime: MutableList<Pair<Long, Long>> = mutableListOf()
    private val workWithoutScreenTime: MutableList<Pair<Long, Long>> = mutableListOf()
    private val socialWithoutScreenTime: MutableList<Pair<Long, Long>> = mutableListOf()


    fun getTotalIdleScreenTime() : Long {
        var totalTime = 0L
        for (pair in idleScreenTime) {
            totalTime += (pair.second - pair.first)
        }
        return totalTime
    }

    fun getTotalIdleWithoutScreenTime() : Long {
        var totalTime = 0L
        for (pair in idleWithoutScreenTime) {
            totalTime += (pair.second - pair.first)
        }
        return totalTime
    }

    fun getTotalWorkScreenTime() : Long {
        var totalTime = 0L
        for (pair in workScreenTime) {
            totalTime += (pair.second - pair.first)
        }
        return totalTime
    }

    fun getTotalWorkWithoutScreenTime() : Long {
        var totalTime = 0L
        for (pair in workWithoutScreenTime) {
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

    fun getTotalSocialWithoutScreenTime() : Long {
        var totalTime = 0L
        for (pair in socialWithoutScreenTime) {
            totalTime += (pair.second - pair.first)
        }
        return totalTime
    }
    
    fun addIdleScreenTime(pair : Pair<Long,Long>) {
        idleScreenTime.add(pair)
    }

    fun addIdleWithoutScreenTime(pair : Pair<Long,Long>) {
        idleWithoutScreenTime.add(pair)
    }

    fun addWorkScreenTime(pair : Pair<Long,Long>) {
        workScreenTime.add(pair)
    }

    fun addWorkWithoutScreenTime(pair : Pair<Long,Long>) {
        workWithoutScreenTime.add(pair)
    }

    fun addSocialScreenTime(pair : Pair<Long,Long>) {
        socialScreenTime.add(pair)
    }

    fun addSocialWithoutScreenTime(pair : Pair<Long,Long>) {
        socialWithoutScreenTime.add(pair)
    }

    override fun toString() : String{
        return "Idle screen time : " + getTotalIdleScreenTime().toString() +
                "\nSocial screen time : " + getTotalSocialScreenTime().toString() +
                "\nWork screen time: " + getTotalWorkScreenTime().toString()
    }

}