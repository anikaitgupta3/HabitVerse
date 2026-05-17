package com.anikaitgupta.habitverse

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


// --- Utility for Time Math ---
object NotificationUtils {
    val CHANNEL_ID = "notification_channel"
    val NOTIFICATION_ID_EXPEDITED = 999
    fun calculateInitialDelay(targetTime: LocalTime): Long {
        val now = LocalDateTime.now()
        val targetToday = LocalDateTime.of(LocalDate.now(), targetTime)
        val finalTarget = if (now.isAfter(targetToday)) targetToday.plusDays(1) else targetToday
        return Duration.between(now, finalTarget).toMinutes()
    }

    fun calculateDelayForTomorrow(targetTime: LocalTime): Long {
        val now = LocalDateTime.now()
        val tomorrowTarget = LocalDateTime.of(LocalDate.now().plusDays(1), targetTime)
        return Duration.between(now, tomorrowTarget).toMinutes()
    }
}