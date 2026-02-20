package com.example.habitverse.data

import android.content.Context
import com.example.habitverse.domain.HabitRepository
import com.example.habitverse.domain.HabitUseCase

interface AppContainer {
    val habitRepository: HabitRepository
    val habitUseCase: HabitUseCase
    val syncManager: SyncManager
}

class AppDataContainer(
    context: Context
) : AppContainer {
    private val database = HabitDatabase.getDatabase(context)
    val habitDao =database.habitDao()
    val remoteDataSource: FakeRemoteDataSource
        get() = FakeRemoteDataSource()
    override val syncManager: SyncManager
        get() = SyncManager(habitDao,remoteDataSource)
    override val habitRepository: HabitRepository by lazy {
        HabitRepositoryImpl(habitDao)
    }
    override val habitUseCase: HabitUseCase
        get() = HabitUseCase(habitRepository)
}