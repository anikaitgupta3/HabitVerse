package com.anikaitgupta.habitverse.di

import android.app.Application
import androidx.room.Room
import com.anikaitgupta.habitverse.data.db.HabitDao
import com.anikaitgupta.habitverse.data.db.HabitDatabase

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