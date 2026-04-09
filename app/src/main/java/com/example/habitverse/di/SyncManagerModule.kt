package com.example.habitverse.di

import com.example.habitverse.data.HabitRepositoryImpl
import com.example.habitverse.data.remote.LogSyncManagerImpl
import com.example.habitverse.data.remote.SyncManagerImpl
import com.example.habitverse.domain.HabitRepository
import com.example.habitverse.domain.SyncManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncManagerModule {

    @Binds
    @HabitSync // Mark this as the Habit version
    @Singleton
    abstract fun bindHabitSync(impl: SyncManagerImpl): SyncManager

    @Binds
    @LogSync // Mark this as the Log version
    @Singleton
    abstract fun bindLogSync(impl: LogSyncManagerImpl): SyncManager
}