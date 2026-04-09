package com.example.habitverse

import com.example.habitverse.data.db.Habit
import com.example.habitverse.data.SyncState
import com.example.habitverse.data.db.HabitLog
import com.example.habitverse.data.remote.HabitDto
import com.example.habitverse.data.remote.HabitLogDto
import com.example.habitverse.domain.HabitDomainModel
import java.time.LocalTime

fun Habit.toDomain(isCompleted: Boolean): HabitDomainModel{
    return HabitDomainModel(id,habitName,habitFrequency,remoteId,showNotification,timeToShowNotification,isCompleted)
}
fun HabitDomainModel.toEntity(): Habit{
    return Habit(this.id ?: 0,habitName,habitFrequency, SyncState.PENDING,false,remoteId,showNotification,timeToShowNotification)
}
fun HabitDomainModel.toEntityInCaseOfDeleted(): Habit{
    return Habit(this.id ?: 0,habitName,habitFrequency, SyncState.PENDING,true,remoteId,showNotification,timeToShowNotification)
}
fun Habit.toHabitDto(): HabitDto{
    return HabitDto(id,habitName,habitFrequency,remoteId,showNotification,timeToShowNotification.toString())
}
fun HabitDto.toHabit(): Habit{
    return Habit(id=0,habitName,habitFrequency, SyncState.SUCCESS,false,remoteId,showNotification,
        LocalTime.parse(timeToShowNotification))
}
fun HabitLog.toHabitLogDto(): HabitLogDto {
    return HabitLogDto(logId,habitId,completionDate,remoteId)
}
fun HabitLogDto.toHabitLog(): HabitLog{
    return HabitLog(logId,habitId,completionDate,SyncState.SUCCESS,false,remoteId)
}