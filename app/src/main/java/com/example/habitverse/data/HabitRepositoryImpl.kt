package com.example.habitverse.data

import com.example.habitverse.data.db.HabitDao
import com.example.habitverse.data.db.HabitLog
import com.example.habitverse.data.db.HabitWithLogs
import com.example.habitverse.di.HabitSync
import com.example.habitverse.di.LogSync
import com.example.habitverse.domain.HabitDomainModel
import com.example.habitverse.domain.HabitRepository
import com.example.habitverse.domain.SyncManager
import com.example.habitverse.toDomain
import com.example.habitverse.toEntity
import com.example.habitverse.toEntityInCaseOfDeleted
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(private val habitDao: HabitDao,@HabitSync private val syncManager: SyncManager,@LogSync private val logSyncManager: SyncManager): HabitRepository {
    override suspend fun insertHabit(habitDomainModel: HabitDomainModel): Long {
        //TODO("Not yet implemented")
        val id=habitDao.insertHabit(habitDomainModel.toEntity())
        syncManager.sync()
        return id

    }

    override suspend fun deleteHabit(habitDomainModel: HabitDomainModel) {
        //TODO("Not yet implemented")
        //habitDao.deleteHabit(habitDomainModel.toEntity())
        habitDao.editHabit(habitDomainModel.toEntityInCaseOfDeleted())
        syncManager.sync()
    }

    override suspend fun editHabit(habitDomainModel: HabitDomainModel) {
        //TODO("Not yet implemented")
        habitDao.editHabit(habitDomainModel.toEntity())
        syncManager.sync()
    }

    /*override fun getAllHabits(): Flow<List<HabitDomainModel>> {
        //TODO("Not yet implemented")
        return habitDao.getAllHabits().map { habitList->
            habitList.map {
                it.toDomain()
            }
        }
    }

    override fun getHabitsById(id: Int): Flow<HabitDomainModel> {
        //TODO("Not yet implemented")
        return habitDao.getHabitsById(id).map {
            it.toDomain()
        }
    }*/

    override fun getAllHabitsWithLogs(): Flow<List<HabitWithLogs>> {
        //TODO("Not yet implemented")
        return habitDao.getAllHabitsWithLogs()
    }

    override suspend fun insertLog(log: HabitLog) {
        //TODO("Not yet implemented")
        habitDao.insertLog(log)
        logSyncManager.sync()
    }

    override suspend fun deleteLog(habitId: Long, date: String) {
        //TODO("Not yet implemented)
        habitDao.deleteLog(habitId,date)
        logSyncManager.sync()
    }

    override suspend fun getHabitWithLogsById(id: Long): HabitWithLogs? {
        //TODO("Not yet implemented")
        return habitDao.getHabitWithLogsById(id)
    }
}