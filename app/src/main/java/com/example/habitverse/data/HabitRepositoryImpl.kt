package com.example.habitverse.data

import com.example.habitverse.domain.HabitRepository
import kotlinx.coroutines.flow.Flow

class HabitRepositoryImpl(private val habitDao: HabitDao): HabitRepository {
    override suspend fun insertHabit(habit: Habit) {
        //TODO("Not yet implemented")
        habitDao.insertHabit(habit)
    }

    override suspend fun deleteHabit(habit: Habit) {
        //TODO("Not yet implemented")
        habitDao.deleteHabit(habit)
    }

    override suspend fun editHabit(habit: Habit) {
        //TODO("Not yet implemented")
        habitDao.editHabit(habit)
    }

    override fun getAllHabits(): Flow<List<Habit>> {
        //TODO("Not yet implemented")
        return habitDao.getAllHabits()
    }

    override fun getHabitsById(id: Int): Flow<Habit> {
        //TODO("Not yet implemented")
        return habitDao.getHabitsById(id)
    }
}