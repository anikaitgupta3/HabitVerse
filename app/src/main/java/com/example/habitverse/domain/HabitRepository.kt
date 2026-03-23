package com.example.habitverse.domain

import com.example.habitverse.data.db.Habit
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    suspend fun insertHabit(habitDomainModel: HabitDomainModel,localImagePath: String?)
    suspend fun deleteHabit(habitDomainModel: HabitDomainModel)
    suspend fun editHabit(habitDomainModel: HabitDomainModel,localImagePath: String?)
    fun getAllHabits(): Flow<List<HabitDomainModel>>
    fun getHabitsById(id: Int): Flow<HabitDomainModel>
}