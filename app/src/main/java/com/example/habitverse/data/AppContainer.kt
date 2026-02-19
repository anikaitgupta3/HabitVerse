package com.example.habitverse.data

import android.content.Context
import com.example.habitverse.domain.HabitRepository
import com.example.habitverse.domain.HabitUseCase

interface AppContainer {
    val habitRepository: HabitRepository
    val habitUseCase: HabitUseCase
}

class AppDataContainer(private val context: Context
) : AppContainer {
    private val database = HabitDatabase.getDatabase(context)
    override val habitRepository: HabitRepository by lazy {
        HabitRepositoryImpl(database.habitDao())
    }
    override val habitUseCase: HabitUseCase
        get() = HabitUseCase(habitRepository)
}