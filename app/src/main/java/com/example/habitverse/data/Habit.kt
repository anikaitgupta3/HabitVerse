package com.example.habitverse.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id:Int=0,
    val habitName: String,
    val habitFrequency: Frequency
)
