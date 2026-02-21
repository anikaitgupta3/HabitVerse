package com.example.habitverse

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HabitVerseApp : Application() {
    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
}