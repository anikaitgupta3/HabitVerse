package com.example.habitverse.di

import com.example.habitverse.data.remote.StorageDataSource
import com.example.habitverse.data.remote.StorageDataSourceImpl
import com.example.habitverse.data.remote.SyncManagerImpl
import com.example.habitverse.domain.SyncManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindStorageDataSource(
        storageDataSourceImpl: StorageDataSourceImpl
    ): StorageDataSource
}