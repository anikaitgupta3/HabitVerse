package com.example.habitverse.data

import android.content.Context
import com.example.habitverse.domain.HabitRepository

interface AppContainer {
    val habitRepository: HabitRepository
}

class AppDataContainer(private val context: Context
) : AppContainer {
    private val database = HabitDatabase.getDatabase(context)
    override val habitRepository: HabitRepository by lazy {
        HabitRepositoryImpl(database.habitDao())
    }
}