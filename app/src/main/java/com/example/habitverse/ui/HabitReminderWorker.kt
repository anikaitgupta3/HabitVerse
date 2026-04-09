package com.example.habitverse.ui

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.habitverse.NotificationUtils
import com.example.habitverse.NotificationUtils.CHANNEL_ID
import com.example.habitverse.R
import com.example.habitverse.data.db.Habit
import com.example.habitverse.data.db.HabitDao
import com.example.habitverse.domain.HabitUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.util.concurrent.TimeUnit

// --- Hilt-Enabled Recursive Worker ---
@HiltWorker
class HabitReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val habitUseCase: HabitUseCase // Direct DAO access or via Repository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val habitId = inputData.getLong("HABIT_ID", -1L)
        val today = LocalDate.now().toString()

//        val data = habitUseCase.getHabitWithLogsById(habitId) ?: return Result.success()
//
//        // 1. STOP if deleted or notifications toggled off
//        /*if (data.habit.isDeleted || !data.habit.showNotification) {
//            return Result.success()
//        }*/
//
//        // 2. Only Notify if NOT already done today
//        val alreadyDone = data.logs.any { it.completionDate == today }
//        if (!alreadyDone) {
//            sendNotification(data.habit)
//        }
        // Inside HabitReminderWorker
        val data = habitUseCase.getHabitWithLogsById(habitId) ?: return Result.success()

        // Even if the habit isn't deleted, some logs might be 'Pending Delete'
        val activeLogs = data.logs.filter { !it.isDeleted }

        val alreadyDone = activeLogs.any { it.completionDate == today }
        if (!alreadyDone) {
            sendNotification(data.habit)
        }

        // 3. RECURSE: Schedule for tomorrow
        val nextDelay = NotificationUtils.calculateDelayForTomorrow(data.habit.timeToShowNotification)
        val nextRequest = OneTimeWorkRequestBuilder<HabitReminderWorker>()
            .setInitialDelay(nextDelay, TimeUnit.MINUTES)
            .setInputData(workDataOf("HABIT_ID" to habitId))
            .addTag("habit_$habitId")
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "habit_reminder_$habitId",
            ExistingWorkPolicy.REPLACE,
            nextRequest
        )

        return Result.success()
    }

    private fun sendNotification(habit: Habit) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.outline_notifications_24)
            .setContentTitle("Pending Habit")
            .setContentText("You have a pending habit to do ${habit.habitName}")
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pIntent)
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(habit.id.toInt(), notificationBuilder.build())

    }
}