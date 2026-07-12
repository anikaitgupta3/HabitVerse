package com.anikaitgupta.habitverse.di

import com.anikaitgupta.habitverse.data.AuthRepositoryImpl
import com.anikaitgupta.habitverse.data.remote.FirebaseRemoteDataSource
import com.anikaitgupta.habitverse.data.remote.LogRemoteDataSource
import com.anikaitgupta.habitverse.data.remote.LogRemoteDataSourceImpl
import com.anikaitgupta.habitverse.data.remote.RemoteDataSource
import com.anikaitgupta.habitverse.domain.AuthRepository
import com.google.firebase.auth.FirebaseAuth
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
    @Singleton
    @Provides
    fun provideLogRemoteDataSource(firestore: FirebaseFirestore): LogRemoteDataSource {
        return LogRemoteDataSourceImpl(firestore)
    }
}