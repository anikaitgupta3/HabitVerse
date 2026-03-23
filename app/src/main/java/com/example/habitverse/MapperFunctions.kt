package com.example.habitverse

import com.example.habitverse.data.ImageSyncState
import com.example.habitverse.data.db.Habit
import com.example.habitverse.data.SyncState
import com.example.habitverse.data.remote.HabitDto
import com.example.habitverse.domain.HabitDomainModel

fun Habit.toDomain(): HabitDomainModel{
    return HabitDomainModel(id,habitName,habitFrequency,imageUrl,localImagePath,remoteId)
}
fun HabitDomainModel.toEntity(): Habit{
    return Habit(this.id ?: 0,habitName,habitFrequency, SyncState.PENDING,false,null,localImagePath,
        ImageSyncState.PENDING,remoteId)
}
fun HabitDomainModel.toEntityInCaseOfNullImagePassed(): Habit{
    return Habit(this.id ?: 0,habitName,habitFrequency, SyncState.PENDING,false,null,localImagePath,
        ImageSyncState.SUCCESS,remoteId)
}

fun HabitDomainModel.toEntityInCaseOfDeleted(): Habit{
    return Habit(this.id ?: 0,habitName,habitFrequency, SyncState.PENDING,true,imageUrl,localImagePath,
        ImageSyncState.PENDING,remoteId)
}
fun HabitDomainModel.toEntityInCaseOfDeletedAndNoImageStored(): Habit{
    return Habit(this.id ?: 0,habitName,habitFrequency, SyncState.PENDING,true,imageUrl,localImagePath,
        ImageSyncState.SUCCESS,remoteId)
}
fun Habit.toHabitDto(imageUrl: String?): HabitDto{
    return HabitDto(id,habitName,habitFrequency,imageUrl,remoteId)
}
fun HabitDto.toHabit(): Habit{
    return Habit(id=0,habitName,habitFrequency, SyncState.SUCCESS,false,imageUrl,null,
        ImageSyncState.SUCCESS,remoteId)
}