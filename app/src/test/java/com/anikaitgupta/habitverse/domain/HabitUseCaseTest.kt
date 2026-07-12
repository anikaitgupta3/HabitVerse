package com.anikaitgupta.habitverse.domain

import app.cash.turbine.test
import com.anikaitgupta.habitverse.data.Frequency
import com.anikaitgupta.habitverse.data.SyncState
import com.anikaitgupta.habitverse.data.db.Habit
import com.anikaitgupta.habitverse.data.db.HabitLog
import com.anikaitgupta.habitverse.data.db.HabitWithLogs
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class HabitUseCaseTest {

    private lateinit var habitRepository: HabitRepository
    private lateinit var habitUseCase: HabitUseCase

    private val fixedDate = LocalDate.of(2024, 1, 20)

    @Before
    fun setUp() {
        habitRepository = mock()
        habitUseCase = HabitUseCase(habitRepository)
        
        mockkStatic(LocalDate::class)
        every { LocalDate.now() } returns fixedDate
    }

    @After
    fun tearDown() {
        unmockkStatic(LocalDate::class)
    }

    private fun createHabitDomainModel(id: Long = 1L) = HabitDomainModel(
        id = id,
        habitName = "Exercise",
        habitFrequency = Frequency.Daily,
        remoteId = "remote_123",
        showNotification = true,
        timeToShowNotification = LocalTime.of(8, 0),
        isCompleted = false,
        createdAt = "2024-01-01"
    )

    @Test
    fun insertHabit_callsRepository() = runTest {
        val habit = createHabitDomainModel()
        whenever(habitRepository.insertHabit(habit)).thenReturn(10L)

        val result = habitUseCase.insertHabit(habit)

        assertEquals(10L, result)
        verify(habitRepository).insertHabit(habit)
    }

    @Test
    fun deleteHabit_callsRepository() = runTest {
        val habit = createHabitDomainModel()
        habitUseCase.deleteHabit(habit)
        verify(habitRepository).deleteHabit(habit)
    }

    @Test
    fun editHabit_callsRepository() = runTest {
        val habit = createHabitDomainModel()
        habitUseCase.editHabit(habit)
        verify(habitRepository).editHabit(habit)
    }

    @Test
    fun getAllHabitsWithLogs_returnsRepositoryFlow() = runTest {
        val expectedFlow = flowOf(emptyList<HabitWithLogs>())
        whenever(habitRepository.getAllHabitsWithLogs()).thenReturn(expectedFlow)

        val result = habitUseCase.getAllHabitsWithLogs()

        assertEquals(expectedFlow, result)
        verify(habitRepository).getAllHabitsWithLogs()
    }

    @Test
    fun insertLog_callsRepositoryWithThreshold() = runTest {
        val log = HabitLog(1, 101, null, "2024-01-20", SyncState.PENDING, false, null)
        val expectedThreshold = fixedDate.minusDays(14).toString() // "2024-01-06"

        habitUseCase.insertLog(log)

        verify(habitRepository).insertLog(eq(log), eq(expectedThreshold))
    }

    @Test
    fun deleteLog_callsRepository() = runTest {
        habitUseCase.deleteLog(101L, "2024-01-20")
        verify(habitRepository).deleteLog(101L, "2024-01-20")
    }

    @Test
    fun getHabitWithLogsById_returnsRepositoryValue() = runTest {
        val habit = Habit(1L, "Exercise", Frequency.Daily, SyncState.SUCCESS, false, "remote_123", true, LocalTime.of(8, 0), "2024-01-01")
        val expected = HabitWithLogs(habit, emptyList())
        whenever(habitRepository.getHabitWithLogsById(1L)).thenReturn(expected)

        val result = habitUseCase.getHabitWithLogsById(1L)

        assertEquals(expected, result)
        verify(habitRepository).getHabitWithLogsById(1L)
    }

    @Test
    fun getAnalyticsData_calculatesCorrectValues() = runTest {
        // Setup mocks for repo
        // logsCurr (Logs this week), logsPrev (Logs last week), totalCurr, totalPrev
        whenever(habitRepository.getCountOfLogsCompletedIn7Days(any(), any())).thenReturn(flowOf(4L))
        whenever(habitRepository.getCountOfLogsCompletedInLast7Days(any(), any())).thenReturn(flowOf(2L))
        whenever(habitRepository.getTotalPossibleCompletionsInLast7Days(any())).thenReturn(flowOf(7L))
        whenever(habitRepository.getTotalPossibleCompletionsInLast7To14Days(any())).thenReturn(flowOf(7L))

        habitUseCase.getAnalyticsData().test {
            val data = awaitItem()
            // (4/7)*100 = 57.14...
            assertEquals(57.14, data.completionRateThisWeek, 0.01)
            // (2/7)*100 = 28.57...
            assertEquals(28.57, data.completionRateLastWeek, 0.01)
            // 57.14 - 28.57 = 28.57
            assertEquals(28.57, data.trend, 0.01)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getRecoveryRate_calculatesAverageGaps() = runTest {
        val habit = Habit(1, "Test", Frequency.Daily, SyncState.SUCCESS, false, null, false, LocalTime.NOON, "2024-01-01")
        val logs = listOf(
            HabitLog(1, 1, null, "2024-01-01", SyncState.SUCCESS, false, null),
            HabitLog(2, 1, null, "2024-01-05", SyncState.SUCCESS, false, null), // gap of 4 days
            HabitLog(3, 1, null, "2024-01-10", SyncState.SUCCESS, false, null)  // gap of 5 days
        )
        val habitWithLogs = HabitWithLogs(habit, logs)
        
        whenever(habitRepository.getAllHabitsWithLogsOrdered()).thenReturn(flowOf(listOf(habitWithLogs)))

        habitUseCase.getRecoveryRate().test {
            val rate = awaitItem()
            // gaps: 4 and 5. Average = (4+5)/2 = 4.5
            assertEquals(4.5, rate, 0.0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun calculateNumberOfHabitsWithStreak_identifies3DayStreaks() = runTest {
        // Fixed date is 2024-01-20
        // 3-day streak range: 2024-01-18 to 2024-01-20
        val habit1 = Habit(1, "Streak", Frequency.Daily, SyncState.SUCCESS, false, null, false, LocalTime.NOON, "2024-01-01")
        val logs1 = listOf(
            HabitLog(1, 1, null, "2024-01-18", SyncState.SUCCESS, false, null),
            HabitLog(2, 1, null, "2024-01-19", SyncState.SUCCESS, false, null),
            HabitLog(3, 1, null, "2024-01-20", SyncState.SUCCESS, false, null)
        )
        
        val habit2 = Habit(2, "No Streak", Frequency.Daily, SyncState.SUCCESS, false, null, false, LocalTime.NOON, "2024-01-01")
        val logs2 = listOf(
            HabitLog(4, 2, null, "2024-01-19", SyncState.SUCCESS, false, null),
            HabitLog(5, 2, null, "2024-01-20", SyncState.SUCCESS, false, null)
        )

        whenever(habitRepository.getAllHabitsWithLogsOrdered()).thenReturn(flowOf(listOf(HabitWithLogs(habit1, logs1), HabitWithLogs(habit2, logs2))))

        habitUseCase.calculateNumberOfHabitsWithStreak().test {
            val count = awaitItem()
            assertEquals(1L, count)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
