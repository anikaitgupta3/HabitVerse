package com.example.habitverse.data.remote

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.habitverse.data.Frequency
import com.example.habitverse.data.SyncState
import java.time.LocalDate
import java.time.LocalTime

data class HabitDto(
    val id: Long = 0,                         // Added = 0
    val habitName: String = "",              // Added = ""
    val habitFrequency: Frequency = Frequency.Daily, // Added a default enum value
    val remoteId: String? = null,            // Added = null
    val showNotification: Boolean = false,
    val timeToShowNotification: String ="00:00",
    val createdAt: String= LocalDate.now().toString()
)
