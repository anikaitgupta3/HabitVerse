package com.anikaitgupta.habitverse.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.anikaitgupta.habitverse.data.db.HabitDao
import com.anikaitgupta.habitverse.domain.AlarmItem
import com.anikaitgupta.habitverse.domain.AlarmScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

class AndroidAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val habitDao: HabitDao
): AlarmScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override suspend fun schedule(item: AlarmItem) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("HABIT_ID", item.habitId)
        }
        val habit = habitDao.getHabitsById(item.habitId).first()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, habit.timeToShowNotification.hour)
        calendar.set(Calendar.MINUTE, habit.timeToShowNotification.minute)
        calendar.set(Calendar.SECOND, 0)

        var timeInMillis = calendar.timeInMillis
        // If the time is in the past today, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            timeInMillis = calendar.timeInMillis
        }
        if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()){
            Log.d("TAG","cannotScheduleExactAlarms")
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                PendingIntent.getBroadcast(
                    context,
                    item.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
        else{
            Log.d("TAG","canScheduleExactAlarms")
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                PendingIntent.getBroadcast(
                    context,
                    item.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }

    }

    override fun cancel(item: AlarmItem) {
        Log.d("TAG","cancel")
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                item.hashCode(),
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    override fun cancelAll(list:List<AlarmItem>) {
        Log.d("TAG","cancelAll")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            alarmManager.cancelAll();
        }
        else{
            list.forEach { item->
                alarmManager.cancel(
                    PendingIntent.getBroadcast(
                        context,
                        item.hashCode(),
                        Intent(context, AlarmReceiver::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
            }
        }
    }
}