package com.anikaitgupta.habitverse.data.db

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.anikaitgupta.habitverse.data.Frequency
import com.anikaitgupta.habitverse.data.SyncState
import java.time.LocalTime
@Keep
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
    val timeToShowNotification: LocalTime,
    val createdAt: String

)