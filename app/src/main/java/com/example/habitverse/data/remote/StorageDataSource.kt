package com.example.habitverse.data.remote

import android.net.Uri

interface StorageDataSource {
    suspend fun uploadImage(userId: String,habitId: String, localPath: String)
    suspend fun deleteImage(userId: String,habitId: String)
    suspend fun getDownloadUrl(userId: String, habitId: String): String
}