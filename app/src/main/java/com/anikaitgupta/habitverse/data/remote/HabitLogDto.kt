package com.anikaitgupta.habitverse.data.remote

data class HabitLogDto(
    val logId:Long=0,
    val habitId:Long=0,
    val completionDate: String="",
    val remoteId: String?=null // Firestore document ID for the log

)