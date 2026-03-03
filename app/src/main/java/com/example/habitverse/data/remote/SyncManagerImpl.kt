package com.example.habitverse.data.remote


import android.util.Log
import com.example.habitverse.data.SyncState
import com.example.habitverse.data.db.Habit
import com.example.habitverse.data.db.HabitDao
import com.example.habitverse.domain.AuthRepository
import com.example.habitverse.domain.SyncManager
import com.example.habitverse.toHabit
import com.example.habitverse.toHabitDto
import javax.inject.Inject

class SyncManagerImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val remoteDataSource: RemoteDataSource,
    private val authRepository: AuthRepository
): SyncManager {

    override suspend fun sync() {
        val pendingAndFailedHabits = habitDao.getAllPendingAndFailedHabits()
        //val isNotEmpty = habitDao.isUserTableNotEmpty()
        for (habit in pendingAndFailedHabits) {
            try {
                syncSingleHabit(habit)
            } catch (e: Exception) {
                markAsFailed(habit)
            }
        }
        /*if(!isNotEmpty){
            insertAllHabits()
        }*/
    }
    override suspend fun cleanRoomAndUpdateRoom(){
        try {
            habitDao.deleteAllHabits()
            insertAllHabits()
        } catch (e: Exception) {
            Log.e("SYNC_ERROR", e.message, e) // NEVER leave a catch block empty!
        }

    }
    override suspend fun cleanRoom(){
        habitDao.deleteAllHabits()
    }
    private suspend fun syncSingleHabit(habit: Habit) {
        val currentUserId = authRepository.getUserId()
        if (habit.isDeleted) {
            if(habit.remoteId !=null) {
                remoteDataSource.deleteHabit(habit.remoteId,currentUserId!!)
            }
            permanentlyDeleteLocal(habit)
            return
        }
        else if (habit.remoteId == null) {
            val remoteId=remoteDataSource.insertHabit(habit.toHabitDto(),currentUserId!!)
            updateLocalWithRemoteId(habit,remoteId)
            //markAsSynced(habit)
            return
        }
        else {
            // Otherwise update existing
            remoteDataSource.updateHabit(habit.toHabitDto(),habit.remoteId,currentUserId!!)
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
        val currentUserId = authRepository.getUserId()
        val habitList=remoteDataSource.getAllHabits(currentUserId!!)
        for(habit in habitList){
            habitDao.insertHabit(habit.toHabit())
        }
    }
}