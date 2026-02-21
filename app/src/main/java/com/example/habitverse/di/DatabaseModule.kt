package com.example.habitverse.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.habitverse.data.HabitDao
import com.example.habitverse.data.HabitDatabase

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule{
    @Singleton
    @Provides
    fun getDatabase(context: Application): HabitDatabase {
        // if the Instance is not null, return it, otherwise create a new database instance.
        return Room.databaseBuilder(context, HabitDatabase::class.java, "habit_database")
                .fallbackToDestructiveMigration(true)
                .build()

    }
    @Singleton
    @Provides
    fun getDao(habitDatabase: HabitDatabase): HabitDao{
        return habitDatabase.habitDao()
    }
}