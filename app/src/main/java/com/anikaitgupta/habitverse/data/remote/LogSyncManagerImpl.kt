package com.anikaitgupta.habitverse.data.remote

import android.util.Log
import com.anikaitgupta.habitverse.data.SyncState
import com.anikaitgupta.habitverse.data.db.HabitDao
import com.anikaitgupta.habitverse.data.db.HabitLog
import com.anikaitgupta.habitverse.domain.AuthRepository
import com.anikaitgupta.habitverse.domain.SyncManager
import com.anikaitgupta.habitverse.toHabitLog
import com.anikaitgupta.habitverse.toHabitLogDto
import javax.inject.Inject

class LogSyncManagerImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val logRemoteDataSource: LogRemoteDataSource,
    private val authRepository: AuthRepository
): SyncManager {
    override suspend fun sync() {
        val pendingAndFailedLogs = habitDao.getPendingLogSyncs()
        for (log in pendingAndFailedLogs) {
            try {
                syncSingleLog(log)
            } catch (e: Exception) {
                markAsFailed(log)
            }
        }
    }

    override suspend fun cleanRoomAndUpdateRoom() {
        try {
            habitDao.deleteAllHabitLogs()
            insertAllLogs()
        } catch (e: Exception) {
            Log.e("LOG_SYNC_ERROR", e.message, e)
        }
    }

    override suspend fun cleanRoom() {
        habitDao.deleteAllHabitLogs()
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return Result.success(Unit)
    }

    private suspend fun syncSingleLog(log: HabitLog) {
        val currentUserId = authRepository.getUserId() ?: return
        if (log.isDeleted) {
            if (log.remoteId != null) {
                logRemoteDataSource.deleteLog(log.remoteId, currentUserId)
            }
            permanentlyDeleteLocal(log)
            return
        } else if (log.remoteId == null) {
            val remoteId = logRemoteDataSource.insertLog(log.toHabitLogDto(), log.habitId, currentUserId)
            updateLocalWithRemoteId(log, remoteId)
            return
        }
    }

    private suspend fun updateLocalWithRemoteId(log: HabitLog, remoteId: String) {
        habitDao.updateLog(
            log.copy(remoteId = remoteId, syncState = SyncState.SUCCESS)
        )
    }


    private suspend fun markAsFailed(log: HabitLog) {
        habitDao.updateLog(
            log.copy(syncState = SyncState.FAILED)
        )
    }

    private suspend fun permanentlyDeleteLocal(log: HabitLog) {
        habitDao.permanentlyDeleteLog(log)
    }

    private suspend fun insertAllLogs() {
        val currentUserId = authRepository.getUserId() ?: return
        val logList = logRemoteDataSource.getAllLogs(currentUserId)
        for (logDto in logList) {
            habitDao.insertLog(logDto.toHabitLog())
        }
    }
}
