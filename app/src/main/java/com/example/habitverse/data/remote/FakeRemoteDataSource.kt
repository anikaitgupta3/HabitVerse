package com.example.habitverse.data.remote

import com.example.habitverse.data.db.Habit
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseRemoteDataSource(private val firestore: FirebaseFirestore) : RemoteDataSource {

    override suspend fun insertHabit(habit: HabitDto): String {
        //TODO("Not yet implemented")
        val docRef = firestore.collection("habits").document()
        val generatedId = docRef.id
        docRef.set(habit.copy(remoteId = generatedId)).await()
        return generatedId
    }

    override suspend fun updateHabit(habit: HabitDto, refId: String) {
        //TODO("Not yet implemented")
        firestore.collection("habits").document(refId).set(habit).await()
    }

    override suspend fun deleteHabit(refId: String) {
        //TODO("Not yet implemented")
        firestore.collection("habits").document(refId).delete().await()
    }

    override suspend fun getAllHabits(): List<HabitDto> {
        //TODO("Not yet implemented")
        val result = firestore.collection("habits").get().await()
        var resultList = mutableListOf<HabitDto>()
        for (habit in result) {
            val mappedHabit = habit.toObject(HabitDto::class.java)
            resultList.add(mappedHabit)
        }
        return resultList.toList()
    }

    override suspend fun getHabitById(id: String): HabitDto? {
        //TODO("Not yet implemented")
        val result = firestore.collection("habits").document(id).get().await()
        val mappedHabit = result.toObject(HabitDto::class.java)
        return mappedHabit
    }

}