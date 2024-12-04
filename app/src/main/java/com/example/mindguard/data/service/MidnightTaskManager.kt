package com.example.mindguard.data.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.mindguard.data.model.User
import com.example.mindguard.data.repository.UserRepository
import java.util.*

class MidnightTaskManager(private val context: Context) {

    private val midnightReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            UserRepository.user.value!!.screenTimeDailyUpdate()
        }
    }

    private val intent = Intent(context, midnightReceiver::class.java)
    private val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleMidnightTask() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        alarmManager.setRepeating(
            AlarmManager.RTC,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelMidnightAlarm() {
        alarmManager.cancel(pendingIntent)

        Log.d("AlarmScheduler", "Midnight alarm cancelled.")
    }
}
