package com.example.habitverse.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.habitverse.data.Frequency
import com.example.habitverse.data.SyncState
import java.time.LocalTime

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long=0,
    val habitName: String,
    val habitFrequency: Frequency,
    val syncState: SyncState,
    val isDeleted: Boolean,
    val remoteId: String?,
    val showNotification: Boolean,
    val timeToShowNotification: LocalTime

)