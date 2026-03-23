package com.example.habitverse.data.remote

import com.example.habitverse.data.Frequency
import com.example.habitverse.data.SyncState

data class HabitDto(
    val id: Int = 0,                         // Added = 0
    val habitName: String = "",              // Added = ""
    val habitFrequency: Frequency = Frequency.Daily, // Added a default enum value
    val imageUrl: String?,
    val remoteId: String? = null             // Added = null
)
