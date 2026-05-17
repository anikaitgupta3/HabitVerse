package com.anikaitgupta.habitverse.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRemoteDataSource(private val firestore: FirebaseFirestore) : RemoteDataSource {

    override suspend fun insertHabit(habit: HabitDto,userId: String): String {
        //TODO("Not yet implemented")
        val docRef = firestore.collection("users").document(userId).collection("habits").document()
        val generatedId = docRef.id
        docRef.set(habit.copy(remoteId = generatedId)).await()
        return generatedId
    }

    override suspend fun updateHabit(habit: HabitDto, refId: String,userId: String) {
        //TODO("Not yet implemented")
        firestore.collection("users").document(userId).collection("habits").document(refId).set(habit).await()
    }

    override suspend fun deleteHabit(refId: String,userId: String) {
        //TODO("Not yet implemented")
        firestore.collection("users").document(userId).collection("habits").document(refId).delete().await()
    }

    override suspend fun getAllHabits(userId: String): List<HabitDto> {
        //TODO("Not yet implemented")
        val result = firestore.collection("users").document(userId).collection("habits").get().await()
        var resultList = mutableListOf<HabitDto>()
        for (habit in result) {
            val mappedHabit = habit.toObject(HabitDto::class.java)
            resultList.add(mappedHabit)
        }
        return resultList.toList()
    }

    override suspend fun getHabitById(id: String,userId: String): HabitDto? {
        //TODO("Not yet implemented")
        val result = firestore.collection("users").document(userId).collection("habits").document(id).get().await()
        val mappedHabit = result.toObject(HabitDto::class.java)
        return mappedHabit
    }

    override suspend fun deleteAccount(userId: String) {
        val habits = firestore.collection("users").document(userId).collection("habits").get().await()
        for (habit in habits) {
            habit.reference.delete().await()
        }
        val logs = firestore.collection("users").document(userId).collection("logs").get().await()
        for (log in logs) {
            log.reference.delete().await()
        }
        firestore.collection("users").document(userId).delete().await()
    }

}