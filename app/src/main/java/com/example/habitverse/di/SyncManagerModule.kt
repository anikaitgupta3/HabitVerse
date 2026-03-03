package com.example.habitverse.di

import com.example.habitverse.data.HabitRepositoryImpl
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
    @Singleton
    abstract fun bindSyncManager(
        syncManagerImpl: SyncManagerImpl
    ): SyncManager
}