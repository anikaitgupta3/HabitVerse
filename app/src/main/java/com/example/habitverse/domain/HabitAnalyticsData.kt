package com.example.habitverse.domain

data class HabitAnalyticsData(
    val logCountThisWeek: Long = 0L,
    val logCountLastWeek: Long = 0L,
    val totalPossibleThisWeek: Long = 0L,
    val totalPossibleLastWeek: Long = 0L,
    val completionRateThisWeek: Double = 0.0,
    val completionRateLastWeek: Double = 0.0,
    val trend: Double = 0.0
)
