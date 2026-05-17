package com.anikaitgupta.habitverse.di

import com.anikaitgupta.habitverse.data.remote.LogSyncManagerImpl
import com.anikaitgupta.habitverse.data.remote.SyncManagerImpl
import com.anikaitgupta.habitverse.domain.SyncManager
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