package com.example.habitverse.data

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.habitverse.NotificationUtils.CHANNEL_ID
import com.example.habitverse.R
import com.example.habitverse.data.db.Habit
import com.example.habitverse.domain.AlarmItem
import com.example.habitverse.domain.AlarmScheduler
import com.example.habitverse.domain.HabitRepository
import com.example.habitverse.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver(): BroadcastReceiver() {
    @Inject
    lateinit var habitRepository: HabitRepository // Use field injection

    @Inject
    lateinit var alarmScheduler: AlarmScheduler



    override fun onReceive(context: Context?, intent: Intent?) {
        val habitId = intent?.getLongExtra("HABIT_ID", -1L)
        val today = LocalDate.now().toString()
        val pendingResult: PendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if(habitId!=null && habitId!=-1L){
                    val data = habitRepository.getHabitWithLogsById(habitId) ?: return@launch
                    val alreadyDone = data.logs.any { it.completionDate == today }
                    if (!alreadyDone && context!=null) {
                        Log.d("TAG","sending notification")
                        sendNotification(data.habit,context)
                    }
                    alarmScheduler.schedule(AlarmItem(habitId))
                }

            }catch (e:Exception){
                e.printStackTrace()
            }
            finally {
                pendingResult.finish()
            }
        }
    }
    private fun sendNotification(habit: Habit,context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.outline_notifications_24)
            .setContentTitle("Pending Habit")
            .setContentText("You have a pending habit to do ${habit.habitName}")
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pIntent)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(habit.id.toInt(), notificationBuilder.build())

    }
}