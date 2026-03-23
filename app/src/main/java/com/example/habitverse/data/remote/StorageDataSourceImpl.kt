package com.example.habitverse.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

class StorageDataSourceImpl @Inject constructor(private val firebaseStorage: FirebaseStorage): StorageDataSource {
    override suspend fun uploadImage(userId: String, habitId: String, localPath: String) {
        // 1. Convert the String path to a File object
        val file = File(localPath)

        // 2. Convert File to Uri
        val fileUri = Uri.fromFile(file)
        //TODO("Not yet implemented")
        firebaseStorage.reference.child("users").child(userId).child("habits").child("${habitId}.jpg").putFile(fileUri).await()
    }

    override suspend fun deleteImage(userId: String, habitId: String) {
        //TODO("Not yet implemented")
        firebaseStorage.reference.child("users").child(userId).child("habits").child("${habitId}.jpg").delete().await()
    }

    override suspend fun getDownloadUrl(userId: String, habitId: String): String {
        //TODO("Not yet implemented")
        return firebaseStorage.reference.child("users").child(userId).child("habits").child("${habitId}.jpg").downloadUrl.await().toString()
    }
}