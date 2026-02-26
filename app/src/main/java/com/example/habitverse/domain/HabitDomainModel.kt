package com.example.habitverse.domain

import com.example.habitverse.data.Frequency

data class HabitDomainModel(
    val id:Int?=null,
    val habitName: String,
    val habitFrequency: Frequency,
    val remoteId: String?
)