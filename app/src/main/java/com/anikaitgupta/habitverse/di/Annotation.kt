package com.anikaitgupta.habitverse.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class HabitSync

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LogSync