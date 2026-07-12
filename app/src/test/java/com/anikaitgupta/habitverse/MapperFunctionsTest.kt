package com.anikaitgupta.habitverse

import com.anikaitgupta.habitverse.data.Frequency
import com.anikaitgupta.habitverse.data.SyncState
import com.anikaitgupta.habitverse.data.db.Habit
import com.anikaitgupta.habitverse.data.db.HabitLog
import com.anikaitgupta.habitverse.data.remote.HabitDto
import com.anikaitgupta.habitverse.data.remote.HabitLogDto
import com.anikaitgupta.habitverse.domain.ChatMessage
import com.anikaitgupta.habitverse.domain.HabitDomainModel
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalTime

class MapperFunctionsTest {
    @Test
    fun toDomain() {
        val habit = Habit(
            id = 1,
            habitName = "Reading",
            habitFrequency = Frequency.Daily,
            syncState = SyncState.SUCCESS,
            isDeleted = false,
            remoteId = "remote_1",
            showNotification = true,
            timeToShowNotification = LocalTime.of(8, 0),
            createdAt = "2023-10-27"
        )
        val domain = habit.toDomain(isCompleted = true)

        assertEquals(habit.id, domain.id)
        assertEquals(habit.habitName, domain.habitName)
        assertEquals(habit.habitFrequency, domain.habitFrequency)
        assertEquals(habit.remoteId, domain.remoteId)
        assertEquals(habit.showNotification, domain.showNotification)
        assertEquals(habit.timeToShowNotification, domain.timeToShowNotification)
        assertTrue(domain.isCompleted)
        assertEquals(habit.createdAt, domain.createdAt)
    }

    @Test
    fun toEntity() {
        val domain = HabitDomainModel(
            id = 1,
            habitName = "Exercise",
            habitFrequency = Frequency.ThreeTimes,
            remoteId = "remote_2",
            showNotification = false,
            timeToShowNotification = LocalTime.of(18, 30),
            isCompleted = false,
            createdAt = "2023-10-26"
        )
        val entity = domain.toEntity()

        assertEquals(domain.id, entity.id)
        assertEquals(domain.habitName, entity.habitName)
        assertEquals(domain.habitFrequency, entity.habitFrequency)
        assertEquals(SyncState.PENDING, entity.syncState)
        assertFalse(entity.isDeleted)
        assertEquals(domain.remoteId, entity.remoteId)
        assertEquals(domain.showNotification, entity.showNotification)
        assertEquals(domain.timeToShowNotification, entity.timeToShowNotification)
        assertEquals(domain.createdAt, entity.createdAt)
    }

    @Test
    fun toEntityInCaseOfDeleted() {
        val domain = HabitDomainModel(
            id = 1,
            habitName = "Exercise",
            habitFrequency = Frequency.ThreeTimes,
            remoteId = "remote_2",
            showNotification = false,
            timeToShowNotification = LocalTime.of(18, 30),
            isCompleted = false,
            createdAt = "2023-10-26"
        )
        val entity = domain.toEntityInCaseOfDeleted()

        assertEquals(domain.id, entity.id)
        assertTrue(entity.isDeleted)
        assertEquals(SyncState.PENDING, entity.syncState)
    }

    @Test
    fun toHabitDto() {
        val habit = Habit(
            id = 1,
            habitName = "Meditate",
            habitFrequency = Frequency.Daily,
            syncState = SyncState.SUCCESS,
            isDeleted = false,
            remoteId = "remote_3",
            showNotification = true,
            timeToShowNotification = LocalTime.of(7, 0),
            createdAt = "2023-10-25"
        )
        val dto = habit.toHabitDto()

        assertEquals(habit.id, dto.id)
        assertEquals(habit.habitName, dto.habitName)
        assertEquals(habit.habitFrequency, dto.habitFrequency)
        assertEquals(habit.remoteId, dto.remoteId)
        assertEquals(habit.showNotification, dto.showNotification)
        assertEquals(habit.timeToShowNotification.toString(), dto.timeToShowNotification)
    }

    @Test
    fun toHabit() {
        val dto = HabitDto(
            id = 1,
            habitName = "Code",
            habitFrequency = Frequency.Everyday,
            remoteId = "remote_4",
            showNotification = true,
            timeToShowNotification = "10:00",
            createdAt = "2023-10-24"
        )
        val entity = dto.toHabit()

        assertEquals(0, entity.id) // Hardcoded in mapper
        assertEquals(dto.habitName, entity.habitName)
        assertEquals(dto.habitFrequency, entity.habitFrequency)
        assertEquals(SyncState.SUCCESS, entity.syncState)
        assertFalse(entity.isDeleted)
        assertEquals(dto.remoteId, entity.remoteId)
        assertEquals(dto.showNotification, entity.showNotification)
        assertEquals(LocalTime.of(10, 0), entity.timeToShowNotification)
        assertEquals(dto.createdAt, entity.createdAt)
    }

    @Test
    fun toHabitLogDto() {
        val log = HabitLog(
            logId = 1,
            habitId = 10,
            habitRemoteId = "remote_habit_1",
            completionDate = "2023-10-27",
            syncState = SyncState.PENDING,
            isDeleted = false,
            remoteId = "remote_log_1"
        )
        val dto = log.toHabitLogDto()

        assertEquals(log.logId, dto.logId)
        assertEquals(log.habitId, dto.habitId)
        assertEquals(log.habitRemoteId, dto.habitRemoteId)
        assertEquals(log.completionDate, dto.completionDate)
        assertEquals(log.remoteId, dto.remoteId)
    }

    @Test
    fun toHabitLog() {
        val dto = HabitLogDto(
            logId = 1,
            habitId = 10,
            habitRemoteId = "remote_habit_1",
            completionDate = "2023-10-27",
            remoteId = "remote_log_1"
        )
        val log = dto.toHabitLog()

        assertEquals(0, log.logId) // Hardcoded in mapper
        assertEquals(dto.habitId, log.habitId)
        assertEquals(dto.habitRemoteId, log.habitRemoteId)
        assertEquals(dto.completionDate, log.completionDate)
        assertEquals(SyncState.SUCCESS, log.syncState)
        assertFalse(log.isDeleted)
        assertEquals(dto.remoteId, log.remoteId)
    }

    @Test
    fun toGeminiInputData() {
        val messages = listOf(
            ChatMessage(role = "user", text = "Hello"),
            ChatMessage(role = "model", text = "Hi there!")
        )
        val geminiData = messages.toGeminiInputData()

        assertEquals(1, geminiData.systemInstruction.parts.size)
        assertTrue(geminiData.systemInstruction.parts[0].text.contains("habit coach"))
        assertEquals(2, geminiData.contents.size)
        assertEquals("user", geminiData.contents[0].role)
        assertEquals("Hello", geminiData.contents[0].parts[0].text)
        assertEquals("model", geminiData.contents[1].role)
        assertEquals("Hi there!", geminiData.contents[1].parts[0].text)
    }
}
