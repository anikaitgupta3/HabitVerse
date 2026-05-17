package com.anikaitgupta.habitverse.domain

interface AlarmScheduler {
    suspend fun schedule(item: AlarmItem)
    fun cancel(item: AlarmItem)
    fun cancelAll(list:List<AlarmItem>)
}