package com.example.habitverse.data

import kotlinx.coroutines.delay
import javax.inject.Inject

class FakeRemoteDataSource @Inject constructor() {

    private val remoteStorage = mutableMapOf<Int, Habit>()
    suspend fun insertHabit(habit: Habit) {
        delay(1000) // simulate network
        //val remoteId = generateRemoteId()
        remoteStorage[habit.id] = habit
    }

    suspend fun updateHabit(habit: Habit) {
        delay(1000)
        remoteStorage[habit.id] = habit
    }

    suspend fun deleteHabit(id: Int) {
        delay(1000)
        remoteStorage.remove(id)
    }
}