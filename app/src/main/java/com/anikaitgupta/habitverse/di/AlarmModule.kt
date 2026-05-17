package com.anikaitgupta.habitverse.di

import com.anikaitgupta.habitverse.data.AndroidAlarmScheduler
import com.anikaitgupta.habitverse.domain.AlarmScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmModule {

    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(
        impl: AndroidAlarmScheduler
    ): AlarmScheduler
}