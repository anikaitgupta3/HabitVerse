package com.example.habitverse.domain

import com.example.habitverse.data.Habit
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    suspend fun insertHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    suspend fun editHabit(habit: Habit)
    fun getAllHabits(): Flow<List<Habit>>
    fun getHabitsById(id: Int): Flow<Habit>
}