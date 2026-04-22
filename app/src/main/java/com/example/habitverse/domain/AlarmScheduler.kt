package com.example.habitverse.domain

import com.example.habitverse.domain.AlarmItem

interface AlarmScheduler {
    suspend fun schedule(item: AlarmItem)
    fun cancel(item: AlarmItem)
    fun cancelAll(list:List<AlarmItem>)
}