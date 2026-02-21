package com.example.habitverse.di

import com.example.habitverse.data.HabitRepositoryImpl
import com.example.habitverse.domain.HabitRepository
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