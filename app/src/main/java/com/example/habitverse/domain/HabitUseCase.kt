package com.example.habitverse.domain

import com.example.habitverse.domain.HabitRepository
import kotlinx.coroutines.flow.Flow

class HabitUseCase(private val habitRepository: HabitRepository) {
    suspend fun insertHabit(habitDomainModel: HabitDomainModel){
        habitRepository.insertHabit(habitDomainModel)
    }
    suspend fun deleteHabit(habitDomainModel: HabitDomainModel){
        habitRepository.deleteHabit(habitDomainModel)
    }
    suspend fun editHabit(habitDomainModel: HabitDomainModel){
        habitRepository.editHabit(habitDomainModel)
    }
    fun getAllHabits(): Flow<List<HabitDomainModel>>{
        return habitRepository.getAllHabits()
    }
    fun getHabitsById(id: Int): Flow<HabitDomainModel>{
        return habitRepository.getHabitsById(id)
    }
}