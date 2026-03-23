package com.example.habitverse.domain

import com.example.habitverse.domain.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HabitUseCase @Inject constructor(private val habitRepository: HabitRepository) {
    suspend fun insertHabit(habitDomainModel: HabitDomainModel,localImagePath: String?){
        habitRepository.insertHabit(habitDomainModel,localImagePath)
    }
    suspend fun deleteHabit(habitDomainModel: HabitDomainModel){
        habitRepository.deleteHabit(habitDomainModel)
    }
    suspend fun editHabit(habitDomainModel: HabitDomainModel,localImagePath: String?){
        habitRepository.editHabit(habitDomainModel,localImagePath)
    }
    fun getAllHabits(): Flow<List<HabitDomainModel>>{
        return habitRepository.getAllHabits()
    }
    fun getHabitsById(id: Int): Flow<HabitDomainModel>{
        return habitRepository.getHabitsById(id)
    }
}