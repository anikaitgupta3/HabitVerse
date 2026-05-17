package com.example.habitverse.domain

import androidx.room.Query
import com.example.habitverse.data.db.Habit
import com.example.habitverse.data.db.HabitLog
import com.example.habitverse.data.db.HabitWithLogs
import com.example.habitverse.data.network.GeminiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.descriptors.PrimitiveKind

interface HabitRepository {
    suspend fun insertHabit(habitDomainModel: HabitDomainModel): Long
    suspend fun deleteHabit(habitDomainModel: HabitDomainModel)
    suspend fun editHabit(habitDomainModel: HabitDomainModel)
    //fun getAllHabits(): Flow<List<HabitDomainModel>>
    //fun getHabitsById(id: Int): Flow<HabitDomainModel>
    fun getAllHabitsWithLogs(): Flow<List<HabitWithLogs>>
    suspend fun insertLog(log: HabitLog,thresholdDate:String)
    suspend fun deleteLog(habitId: Long, date: String)
    suspend fun getHabitWithLogsById(id: Long): HabitWithLogs?
    fun getCountOfLogsCompletedIn7Days(currentDate:String,date7DaysBack: String): Flow<Long>
    fun getCountOfLogsCompletedInLast7Days(date7DaysBack: String,date14DaysBack: String): Flow<Long>
    fun getTotalPossibleCompletionsInLast7Days(currentDate: String): Flow<Long>
    fun getTotalPossibleCompletionsInLast7To14Days(date7DaysBack: String):Flow<Long>
    fun getAllHabitsWithLogsOrdered(): Flow<List<HabitWithLogs>>
    suspend fun getTipsForHabitImprovement(apiKey:String,string: String): GeminiResult
}