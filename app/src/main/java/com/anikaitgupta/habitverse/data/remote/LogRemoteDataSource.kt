package com.anikaitgupta.habitverse.data.remote

interface LogRemoteDataSource {
    suspend fun insertLog(log: HabitLogDto, habitId: Long, userId: String): String
    suspend fun deleteLog(logId: String, userId: String)
    suspend fun getAllLogs(userId: String): List<HabitLogDto>
}
