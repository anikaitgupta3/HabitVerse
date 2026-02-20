package com.example.habitverse.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert
    suspend fun insertHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Update
    suspend fun editHabit(habit: Habit)

    @Query("select* from habits where isDeleted = 0")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("select* from habits where id = :id")
    fun getHabitsById(id: Int):Flow<Habit>

    @Query("select* from habits where syncState = 'PENDING'")
    fun getAllPendingHabits():List<Habit>
}