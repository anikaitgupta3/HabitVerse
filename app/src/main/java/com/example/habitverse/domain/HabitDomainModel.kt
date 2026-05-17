package com.example.habitverse.domain

import com.example.habitverse.data.Frequency
import java.time.LocalTime

data class HabitDomainModel(
    val id: Long?=null,
    val habitName: String,
    val habitFrequency: Frequency,
    val remoteId: String?,
    val showNotification: Boolean,
    val timeToShowNotification: LocalTime,
    val isCompleted: Boolean,
    val createdAt: String
)