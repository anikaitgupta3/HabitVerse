package com.example.habitverse

import com.example.habitverse.data.db.Habit
import com.example.habitverse.data.SyncState
import com.example.habitverse.data.remote.HabitDto
import com.example.habitverse.domain.HabitDomainModel

fun Habit.toDomain(): HabitDomainModel{
    return HabitDomainModel(id,habitName,habitFrequency,remoteId)
}
fun HabitDomainModel.toEntity(): Habit{
    return Habit(this.id ?: 0,habitName,habitFrequency, SyncState.PENDING,false,remoteId)
}
fun HabitDomainModel.toEntityInCaseOfDeleted(): Habit{
    return Habit(this.id ?: 0,habitName,habitFrequency, SyncState.PENDING,true,remoteId)
}
fun Habit.toHabitDto(): HabitDto{
    return HabitDto(id,habitName,habitFrequency,remoteId)
}
fun HabitDto.toHabit(): Habit{
    return Habit(id=0,habitName,habitFrequency, SyncState.SUCCESS,false,remoteId)
}