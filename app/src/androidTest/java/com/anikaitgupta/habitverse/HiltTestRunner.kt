package com.anikaitgupta.habitverse

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.testing.HiltTestApplication

class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }

    override fun callApplicationOnCreate(app: Application?) {
        // Initialize WorkManager here instead of newApplication to ensure context is ready
        app?.let {
            try {
                WorkManager.initialize(it, Configuration.Builder().build())
            } catch (e: IllegalStateException) {
                // Already initialized
            }
        }
        super.callApplicationOnCreate(app)
    }
}
