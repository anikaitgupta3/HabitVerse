package com.anikaitgupta.habitverse.di

import com.anikaitgupta.habitverse.data.HabitRepositoryImpl
import com.anikaitgupta.habitverse.domain.HabitRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMyRepository(
        myRepositoryImpl: HabitRepositoryImpl
    ): HabitRepository
}