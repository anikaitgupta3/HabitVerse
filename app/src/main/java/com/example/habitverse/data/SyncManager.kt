package com.example.habitverse.data

class SyncManager(
    private val habitDao: HabitDao,
    private val remoteDataSource: FakeRemoteDataSource
) {
    suspend fun sync() {
        val pendingHabits = habitDao.getAllPendingHabits()
        for (habit in pendingHabits) {
            try {
                syncSingleHabit(habit)
            } catch (e: Exception) {
                markAsFailed(habit)
            }
        }
    }
    private suspend fun syncSingleHabit(habit: Habit) {
        if (habit.isDeleted) {
            remoteDataSource.deleteHabit(habit.id)
            permanentlyDeleteLocal(habit)
            return
        }
        else if (habit.remoteId == null) {
            remoteDataSource.insertHabit(habit)
            updateLocalWithRemoteId(habit)
            markAsSynced(habit)
            return
        }
        else {
            // Otherwise update existing
            remoteDataSource.updateHabit(habit)
            markAsSynced(habit)
        }
    }
    private suspend fun updateLocalWithRemoteId(habit: Habit){
        habitDao.editHabit(
            habit.copy(remoteId = "abc")
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
}