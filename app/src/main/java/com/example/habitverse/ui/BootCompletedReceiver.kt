package com.example.habitverse.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.habitverse.domain.AlarmItem
import com.example.habitverse.domain.AlarmScheduler
import com.example.habitverse.domain.HabitUseCase
import com.example.habitverse.toDomain
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver: BroadcastReceiver() {

    @Inject
    lateinit var habitUseCase: HabitUseCase
    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            //println("Hello world, I'm booted up!")
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                val listOfHabits = habitUseCase.getAllHabitsWithLogs().first().map { it.habit }
                listOfHabits.forEach{ habit->
                    //handleAlarmManagement(it.toDomain(false),isDeleted = false)
                    alarmScheduler.schedule(AlarmItem(habit.id))
                }
                pendingResult.finish()
            }
        }
    }
}