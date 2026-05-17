package com.example.habitverse.domain

interface SyncManager {
    suspend fun sync()
    suspend fun cleanRoomAndUpdateRoom()
    suspend fun cleanRoom()
    suspend fun deleteAccount(): Result<Unit>
}