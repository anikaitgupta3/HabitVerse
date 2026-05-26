package com.anikaitgupta.habitverse.data

import android.util.Log
import com.anikaitgupta.habitverse.data.db.HabitDao
import com.anikaitgupta.habitverse.data.db.HabitLog
import com.anikaitgupta.habitverse.data.db.HabitWithLogs
import com.anikaitgupta.habitverse.data.network.GeminiApi
import com.anikaitgupta.habitverse.data.network.GeminiResult
import com.anikaitgupta.habitverse.di.HabitSync
import com.anikaitgupta.habitverse.di.LogSync
import com.anikaitgupta.habitverse.domain.ChatMessage
import com.anikaitgupta.habitverse.domain.HabitDomainModel
import com.anikaitgupta.habitverse.domain.HabitRepository
import com.anikaitgupta.habitverse.domain.SyncManager
import com.anikaitgupta.habitverse.toEntity
import com.anikaitgupta.habitverse.toEntityInCaseOfDeleted
import com.anikaitgupta.habitverse.toGeminiInputData
import kotlinx.coroutines.flow.Flow
import okio.IOException
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(private val habitDao: HabitDao,@HabitSync private val syncManager: SyncManager,@LogSync private val logSyncManager: SyncManager,private val geminiApi: GeminiApi): HabitRepository {
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

    override suspend fun insertLog(log: HabitLog,thresholdDate:String) {
        //TODO("Not yet implemented")
        habitDao.insertLog(log)
        habitDao.markOldLogsAsDeleted(thresholdDate)
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

    override fun getCountOfLogsCompletedIn7Days(
        currentDate: String,
        date7DaysBack: String
    ): Flow<Long> {
        return habitDao.getCountOfLogsCompletedIn7Days(currentDate,date7DaysBack)
    }

    override fun getCountOfLogsCompletedInLast7Days(
        date7DaysBack: String,
        date14DaysBack: String
    ): Flow<Long> {
        return habitDao.getCountOfLogsCompletedInLast7Days(date7DaysBack,date14DaysBack)
    }

    override fun getTotalPossibleCompletionsInLast7Days(currentDate: String): Flow<Long> {
        return habitDao.getTotalPossibleCompletionsInLast7Days(currentDate)
    }

    override fun getTotalPossibleCompletionsInLast7To14Days(date7DaysBack: String): Flow<Long> {
        return habitDao.getTotalPossibleCompletionsInLast7To14Days(date7DaysBack)
    }

    override fun getAllHabitsWithLogsOrdered(): Flow<List<HabitWithLogs>> {
        return habitDao.getAllHabitsWithLogsOrdered()
    }

    override suspend fun getTipsForHabitImprovement(apiKey:String,chatMessageList: List<ChatMessage>): GeminiResult {
        //TODO("Not yet implemented")
        //emit(GeminiResult.Loading)
        try {
            val geminiResponse = geminiApi.getTipsForHabit(apiKey, chatMessageList.toGeminiInputData())
            val geminiBody =
                geminiResponse.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!geminiResponse.isSuccessful || geminiBody.isNullOrEmpty()) {
                //emit(GeminiResult.Error("Sorry we can't help you with this"))
                //return@flow
                return GeminiResult.Error("Sorry we can't help you with this")
            }
            return GeminiResult.Success(geminiBody)
        } catch (e: IOException) {
            return GeminiResult.Error("Network failure: Please check your connection")
        } catch (e: Exception) {
            Log.e("Error","An unexpected error occurred: ${e.localizedMessage}")
            return GeminiResult.Error("An unexpected error occurred: ${e.localizedMessage}")

        }
    }

}