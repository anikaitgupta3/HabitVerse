package com.anikaitgupta.habitverse.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.anikaitgupta.habitverse.data.SyncState

@Entity(tableName = "habit_logs",
    indices = [Index(value = ["habitId", "completionDate"], unique = true)])
data class HabitLog(
    @PrimaryKey(autoGenerate = true)
    val logId:Long=0,
    val habitId:Long,
    val completionDate: String,
    val syncState: SyncState,
    val isDeleted: Boolean,
    val remoteId: String? // Firestore document ID for the log

)
