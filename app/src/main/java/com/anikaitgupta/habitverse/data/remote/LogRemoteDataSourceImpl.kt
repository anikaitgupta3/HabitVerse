package com.anikaitgupta.habitverse.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LogRemoteDataSourceImpl(private val firestore: FirebaseFirestore): LogRemoteDataSource {
    override suspend fun insertLog(
        log: HabitLogDto,
        habitId: Long,
        userId: String
    ): String {
        //TODO("Not yet implemented")
        val documentReference = firestore.collection("users")
            .document(userId).collection("logs").document()
        val generatedId = documentReference.id
        documentReference.set(log.copy(remoteId = generatedId)).await()
        return generatedId

    }

    override suspend fun deleteLog(logId: String, userId: String) {
        //TODO("Not yet implemented")
        firestore.collection("users").document(userId).collection("logs").document(logId).delete().await()
    }

    override suspend fun getAllLogs(userId: String): List<HabitLogDto> {
        //TODO("Not yet implemented")
        val result = firestore.collection("users").document(userId).collection("logs").get().await()
        var resultList = mutableListOf<HabitLogDto>()
        for (log in result) {
            val mappedLog = log.toObject(HabitLogDto::class.java)
            resultList.add(mappedLog)
        }
        return resultList.toList()
    }
}