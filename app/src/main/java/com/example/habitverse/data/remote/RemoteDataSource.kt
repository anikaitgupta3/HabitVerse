package com.example.habitverse.data.remote

import com.example.habitverse.data.db.Habit

interface RemoteDataSource {
    suspend fun insertHabit(habit: HabitDto,userId: String): String
    suspend fun updateHabit(habit: HabitDto,refId: String,userId: String)
    suspend fun deleteHabit(refId: String,userId: String)
    suspend fun getAllHabits(userId: String): List<HabitDto>
    suspend fun getHabitById(id: String,userId: String): HabitDto?

    suspend fun deleteAccount(userId: String)

}