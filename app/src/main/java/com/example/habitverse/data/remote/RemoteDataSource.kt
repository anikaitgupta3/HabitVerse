package com.example.habitverse.data.remote

import com.example.habitverse.data.db.Habit

interface RemoteDataSource {
    suspend fun insertHabit(habit: HabitDto): String
    suspend fun updateHabit(habit: HabitDto,refId: String)
    suspend fun deleteHabit(refId: String)
    suspend fun getAllHabits(): List<HabitDto>
    suspend fun getHabitById(id: String): HabitDto?

}