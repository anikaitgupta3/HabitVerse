package com.example.habitverse

import android.app.Application
import com.example.habitverse.data.AppContainer
import com.example.habitverse.data.AppDataContainer

class HabitVerseApp : Application() {
    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}