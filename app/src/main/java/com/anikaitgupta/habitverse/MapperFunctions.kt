package com.anikaitgupta.habitverse

import com.anikaitgupta.habitverse.data.db.Habit
import com.anikaitgupta.habitverse.data.SyncState
import com.anikaitgupta.habitverse.data.db.HabitLog
import com.anikaitgupta.habitverse.data.network.GeminiInputData.Content
import com.anikaitgupta.habitverse.data.network.GeminiInputData.GeminiInputData
import com.anikaitgupta.habitverse.data.network.GeminiInputData.Part
import com.anikaitgupta.habitverse.data.network.GeminiInputData.SystemInstruction
import com.anikaitgupta.habitverse.data.remote.HabitDto
import com.anikaitgupta.habitverse.data.remote.HabitLogDto
import com.anikaitgupta.habitverse.domain.HabitDomainModel
import com.anikaitgupta.habitverse.domain.ChatMessage
import java.time.LocalTime

fun Habit.toDomain(isCompleted: Boolean): HabitDomainModel{
    return HabitDomainModel(id,habitName,habitFrequency,remoteId,showNotification,timeToShowNotification,isCompleted,createdAt)
}
fun HabitDomainModel.toEntity(): Habit{
    return Habit(this.id ?: 0,habitName,habitFrequency, SyncState.PENDING,false,remoteId,showNotification,timeToShowNotification,createdAt)
}
fun HabitDomainModel.toEntityInCaseOfDeleted(): Habit{
    return Habit(this.id ?: 0,habitName,habitFrequency, SyncState.PENDING,true,remoteId,showNotification,timeToShowNotification,createdAt)
}
fun Habit.toHabitDto(): HabitDto{
    return HabitDto(id,habitName,habitFrequency,remoteId,showNotification,timeToShowNotification.toString())
}
fun HabitDto.toHabit(): Habit{
    return Habit(id=0,habitName,habitFrequency, SyncState.SUCCESS,false,remoteId,showNotification,
        LocalTime.parse(timeToShowNotification),createdAt)
}
fun HabitLog.toHabitLogDto(): HabitLogDto {
    return HabitLogDto(logId,habitId,habitRemoteId,completionDate,remoteId)
}
fun HabitLogDto.toHabitLog(): HabitLog{
    return HabitLog(logId=0,habitId,habitRemoteId,completionDate,SyncState.SUCCESS,false,remoteId)
}
//fun String.toGeminiInputData(): GeminiInputData{
//    return GeminiInputData(listOf(Content(listOf(Part(this)))))
//}
fun List<ChatMessage>.toGeminiInputData(): GeminiInputData {
    return GeminiInputData(
        systemInstruction = SystemInstruction(
            parts = listOf(
                Part(
                    "You are a helpful habit coach. Keep responses under 80 words."
                )
            )
        ),
        contents = map {

            Content(
                role = it.role,
                parts = listOf(
                    Part(it.text)
                )
            )
        }
    )
}