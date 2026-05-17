package com.anikaitgupta.habitverse.ui

// --- Hilt-Enabled Recursive Worker ---
/*@HiltWorker
class HabitReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val habitUseCase: HabitUseCase // Direct DAO access or via Repository
) : CoroutineWorker(context, params) {
    // 1. Override this for Android 11 and below support
//    override suspend fun getForegroundInfo(): ForegroundInfo {
//        return ForegroundInfo(
//            NOTIFICATION_ID_EXPEDITED, // A unique constant Int, e.g., 999
//            createExpeditedNotification()
//        )
//    }

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
            //.setExpedited(RUN_AS_NON_EXPEDITED_WORK_REQUEST) // CRITICAL
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
//    private fun createExpeditedNotification(): Notification {
//        // Ensuring a silent channel for the "Sync/Check" phase
//        val silentChannelId = "background_sync_channel"
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                silentChannelId,
//                "System Processing",
//                NotificationManager.IMPORTANCE_LOW // Silent!
//            )
//            val manager = applicationContext.getSystemService(NotificationManager::class.java)
//            manager.createNotificationChannel(channel)
//        }
//
//        return NotificationCompat.Builder(applicationContext, silentChannelId)
//            .setSmallIcon(R.drawable.outline_notifications_24)
//            .setContentTitle("Processing Habit") // Keep it brief
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .build()
//    }
}*/