package com.example.habitverse.data.remote

import com.example.habitverse.data.Frequency
import com.example.habitverse.data.SyncState

data class HabitDto
    (val id:Int,
     val habitName: String,
     val habitFrequency: Frequency,
     val remoteId: String?
    )
