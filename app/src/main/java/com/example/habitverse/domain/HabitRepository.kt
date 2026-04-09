package com.example.habitverse.domain

import com.example.habitverse.data.db.Habit
import com.example.habitverse.data.db.HabitLog
import com.example.habitverse.data.db.HabitWithLogs
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    suspend fun insertHabit(habitDomainModel: HabitDomainModel): Long
    suspend fun deleteHabit(habitDomainModel: HabitDomainModel)
    suspend fun editHabit(habitDomainModel: HabitDomainModel)
    //fun getAllHabits(): Flow<List<HabitDomainModel>>
    //fun getHabitsById(id: Int): Flow<HabitDomainModel>
    fun getAllHabitsWithLogs(): Flow<List<HabitWithLogs>>
    suspend fun insertLog(log: HabitLog)
    suspend fun deleteLog(habitId: Long, date: String)
    suspend fun getHabitWithLogsById(id: Long): HabitWithLogs?
}