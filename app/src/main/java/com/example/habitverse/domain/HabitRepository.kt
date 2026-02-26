package com.example.habitverse.domain

import com.example.habitverse.data.db.Habit
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    suspend fun insertHabit(habitDomainModel: HabitDomainModel)
    suspend fun deleteHabit(habitDomainModel: HabitDomainModel)
    suspend fun editHabit(habitDomainModel: HabitDomainModel)
    fun getAllHabits(): Flow<List<HabitDomainModel>>
    fun getHabitsById(id: Int): Flow<HabitDomainModel>
}