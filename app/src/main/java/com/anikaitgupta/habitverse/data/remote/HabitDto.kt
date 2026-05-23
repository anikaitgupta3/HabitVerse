package com.anikaitgupta.habitverse.data.remote

import androidx.annotation.Keep
import com.anikaitgupta.habitverse.data.Frequency
import java.time.LocalDate
@Keep
data class HabitDto(
    val id: Long = 0,                         // Added = 0
    val habitName: String = "",              // Added = ""
    val habitFrequency: Frequency = Frequency.Daily, // Added a default enum value
    val remoteId: String? = null,            // Added = null
    val showNotification: Boolean = false,
    val timeToShowNotification: String ="00:00",
    val createdAt: String= LocalDate.now().toString()
)
