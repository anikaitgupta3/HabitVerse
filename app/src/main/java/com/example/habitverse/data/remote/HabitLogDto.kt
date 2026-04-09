package com.example.habitverse.data.remote

import androidx.room.PrimaryKey
import com.example.habitverse.data.SyncState

data class HabitLogDto(
    val logId:Long=0,
    val habitId:Long=0,
    val completionDate: String="",
    val remoteId: String?=null // Firestore document ID for the log

)