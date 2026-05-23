package com.anikaitgupta.habitverse.data.db

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Relation

@Keep
data class HabitWithLogs(
    @Embedded val habit: Habit,
    @Relation(
        parentColumn = "id",
        entityColumn = "habitId"
    )
    val logs: List<HabitLog>
)