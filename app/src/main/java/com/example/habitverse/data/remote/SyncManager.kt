package com.example.habitverse.data.remote


import com.example.habitverse.data.SyncState
import com.example.habitverse.data.db.Habit
import com.example.habitverse.data.db.HabitDao
import com.example.habitverse.toHabit
import com.example.habitverse.toHabitDto
import javax.inject.Inject

class SyncManager @Inject constructor(
    private val habitDao: HabitDao,
    private val remoteDataSource: RemoteDataSource
) {
    suspend fun sync() {
        val pendingAndFailedHabits = habitDao.getAllPendingAndFailedHabits()
        val isNotEmpty = habitDao.isUserTableNotEmpty()
        for (habit in pendingAndFailedHabits) {
            try {
                syncSingleHabit(habit)
            } catch (e: Exception) {
                markAsFailed(habit)
            }
        }
        if(!isNotEmpty){
            insertAllHabits()
        }
    }
    private suspend fun syncSingleHabit(habit: Habit) {
        if (habit.isDeleted) {
            if(habit.remoteId !=null) {
                remoteDataSource.deleteHabit(habit.remoteId)
            }
            permanentlyDeleteLocal(habit)
            return
        }
        else if (habit.remoteId == null) {
            val remoteId=remoteDataSource.insertHabit(habit.toHabitDto())
            updateLocalWithRemoteId(habit,remoteId)
            //markAsSynced(habit)
            return
        }
        else {
            // Otherwise update existing
            remoteDataSource.updateHabit(habit.toHabitDto(),habit.remoteId)
            markAsSynced(habit)
        }
    }
    private suspend fun updateLocalWithRemoteId(habit: Habit,remoteId: String){
        habitDao.editHabit(
            habit.copy(remoteId = remoteId,syncState = SyncState.SUCCESS)
        )
    }
    private suspend fun markAsSynced(habit: Habit) {
        habitDao.editHabit(
            habit.copy(
                syncState = SyncState.SUCCESS
            )
        )
    }
    private suspend fun markAsFailed(habit: Habit) {
        habitDao.editHabit(
            habit.copy(
                syncState = SyncState.FAILED
            )
        )
    }
    private suspend fun permanentlyDeleteLocal(habit: Habit) {
        habitDao.deleteHabit(habit)
    }
    private suspend fun insertAllHabits(){
        val habitList=remoteDataSource.getAllHabits()
        for(habit in habitList){
            habitDao.insertHabit(habit.toHabit())
        }
    }
}