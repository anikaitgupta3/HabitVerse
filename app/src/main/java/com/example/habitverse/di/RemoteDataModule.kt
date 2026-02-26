package com.example.habitverse.di

import com.example.habitverse.data.remote.FirebaseRemoteDataSource
import com.example.habitverse.data.remote.RemoteDataSource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteDataModule {
    @Singleton
    @Provides
    fun getFirebaseInstance(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    @Singleton
    @Provides
    fun provideRemoteDataSource(firestore: FirebaseFirestore): RemoteDataSource{
        return FirebaseRemoteDataSource(firestore)
    }
}