package com.example.habitverse.domain

import com.example.habitverse.data.db.HabitLog
import com.example.habitverse.data.db.HabitWithLogs
import com.example.habitverse.domain.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HabitUseCase @Inject constructor(private val habitRepository: HabitRepository) {
    suspend fun insertHabit(habitDomainModel: HabitDomainModel): Long{
        return habitRepository.insertHabit(habitDomainModel)
    }
    suspend fun deleteHabit(habitDomainModel: HabitDomainModel){
        habitRepository.deleteHabit(habitDomainModel)
    }
    suspend fun editHabit(habitDomainModel: HabitDomainModel){
        habitRepository.editHabit(habitDomainModel)
    }
    /*fun getAllHabits(): Flow<List<HabitDomainModel>>{
        return habitRepository.getAllHabits()
    }
    fun getHabitsById(id: Int): Flow<HabitDomainModel>{
        return habitRepository.getHabitsById(id)
    }*/
     fun getAllHabitsWithLogs(): Flow<List<HabitWithLogs>> {
        //TODO("Not yet implemented")
        return habitRepository.getAllHabitsWithLogs()
    }

     suspend fun insertLog(log: HabitLog) {
        //TODO("Not yet implemented")
        habitRepository.insertLog(log)
    }

     suspend fun deleteLog(habitId: Long, date: String) {
        //TODO("Not yet implemented)
        habitRepository.deleteLog(habitId,date)
    }

     suspend fun getHabitWithLogsById(id: Long): HabitWithLogs? {
        //TODO("Not yet implemented")
        return habitRepository.getHabitWithLogsById(id)
    }
}