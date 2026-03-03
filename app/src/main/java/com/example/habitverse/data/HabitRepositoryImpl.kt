package com.example.habitverse.data

import com.example.habitverse.data.db.HabitDao
import com.example.habitverse.domain.HabitDomainModel
import com.example.habitverse.domain.HabitRepository
import com.example.habitverse.domain.SyncManager
import com.example.habitverse.toDomain
import com.example.habitverse.toEntity
import com.example.habitverse.toEntityInCaseOfDeleted
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(private val habitDao: HabitDao,private val syncManager: SyncManager): HabitRepository {
    override suspend fun insertHabit(habitDomainModel: HabitDomainModel) {
        //TODO("Not yet implemented")
        habitDao.insertHabit(habitDomainModel.toEntity())
        syncManager.sync()

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

    override fun getAllHabits(): Flow<List<HabitDomainModel>> {
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
    }
}