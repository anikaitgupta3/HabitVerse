package com.example.habitverse

import com.example.habitverse.data.Habit
import com.example.habitverse.domain.HabitDomainModel

fun Habit.toDomain(): HabitDomainModel{
    return HabitDomainModel(id,habitName,habitFrequency)
}
fun HabitDomainModel.toEntity(): Habit{
    return Habit(this.id ?: 0,habitName,habitFrequency)
}