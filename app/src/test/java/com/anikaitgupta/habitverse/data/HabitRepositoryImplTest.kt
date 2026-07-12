package com.anikaitgupta.habitverse.data

import android.util.Log
import com.anikaitgupta.habitverse.data.db.Habit
import com.anikaitgupta.habitverse.data.db.HabitDao
import com.anikaitgupta.habitverse.data.db.HabitLog
import com.anikaitgupta.habitverse.data.db.HabitWithLogs
import com.anikaitgupta.habitverse.data.network.GeminiApi
import com.anikaitgupta.habitverse.data.network.GeminiResponse.Candidate
import com.anikaitgupta.habitverse.data.network.GeminiResponse.ContentResponse
import com.anikaitgupta.habitverse.data.network.GeminiResponse.GeminiResponse
import com.anikaitgupta.habitverse.data.network.GeminiResponse.PartResponse
import com.anikaitgupta.habitverse.data.network.GeminiResult
import com.anikaitgupta.habitverse.domain.ChatMessage
import com.anikaitgupta.habitverse.domain.HabitDomainModel
import com.anikaitgupta.habitverse.domain.SyncManager
import com.anikaitgupta.habitverse.toEntity
import com.anikaitgupta.habitverse.toEntityInCaseOfDeleted
import com.anikaitgupta.habitverse.toGeminiInputData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.IOException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.time.LocalTime

class HabitRepositoryImplTest {

    private lateinit var habitDao: HabitDao
    private lateinit var habitSyncManager: SyncManager
    private lateinit var logSyncManager: SyncManager
    private lateinit var geminiApi: GeminiApi
    private lateinit var repository: HabitRepositoryImpl

    @Before
    fun setUp() {
        habitDao = mock()
        habitSyncManager = mock()
        logSyncManager = mock()
        geminiApi = mock()
        repository = HabitRepositoryImpl(habitDao, habitSyncManager, logSyncManager, geminiApi)

        // Mock Android Log to avoid "Method not mocked" error
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        //every { Log.w(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
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

    // 1. Habit Operations

    @Test
    fun insertHabit_success_insertsToDaoAndTriggersHabitSync() = runTest {
        val habit = createHabitDomainModel()
        whenever(habitDao.insertHabit(any())).thenReturn(10L)

        val result = repository.insertHabit(habit)

        assertEquals(10L, result)
        verify(habitDao).insertHabit(eq(habit.toEntity()))
        verify(habitSyncManager).sync()
        verify(logSyncManager, never()).sync()
    }

    @Test
    fun deleteHabit_success_updatesDaoToDeletedStateAndTriggersHabitSync() = runTest {
        val habit = createHabitDomainModel()

        repository.deleteHabit(habit)

        verify(habitDao).editHabit(eq(habit.toEntityInCaseOfDeleted()))
        verify(habitSyncManager).sync()
        verify(logSyncManager, never()).sync()
    }

    @Test
    fun editHabit_success_updatesDaoAndTriggersHabitSync() = runTest {
        val habit = createHabitDomainModel()

        repository.editHabit(habit)

        verify(habitDao).editHabit(eq(habit.toEntity()))
        verify(habitSyncManager).sync()
        verify(logSyncManager, never()).sync()
    }

    // 2. Habit Log Operations

    @Test
    fun insertLog_success_insertsLogMarksOldLogsDeletedAndTriggersLogSync() = runTest {
        val log = HabitLog(logId = 1, habitId = 101, completionDate = "2024-01-01", syncState = SyncState.PENDING, isDeleted = false, remoteId = null)
        val threshold = "2023-12-25"

        repository.insertLog(log, threshold)

        verify(habitDao).insertLog(eq(log))
        verify(habitDao).markOldLogsAsDeleted(eq(threshold))
        verify(logSyncManager).sync()
        verify(habitSyncManager, never()).sync()
    }

    @Test
    fun deleteLog_success_deletesFromDaoAndTriggersLogSync() = runTest {
        repository.deleteLog(101L, "2024-01-01")

        verify(habitDao).deleteLog(eq(101L), eq("2024-01-01"))
        verify(logSyncManager).sync()
        verify(habitSyncManager, never()).sync()
    }

    // 3. Gemini API Integration

    @Test
    fun getTipsForHabitImprovement_success_returnsSuccessResult() = runTest {
        val chatMessages = listOf(ChatMessage("user", "Help me exercise"))
        val geminiResponse = GeminiResponse(
            candidates = listOf(
                Candidate(
                    content = ContentResponse(
                        parts = listOf(PartResponse(text = "Start small."))
                    )
                )
            )
        )
        whenever(geminiApi.getTipsForHabit(any(), any())).thenReturn(Response.success(geminiResponse))

        val result = repository.getTipsForHabitImprovement("apiKey", chatMessages)

        assertTrue(result is GeminiResult.Success)
        assertEquals("Start small.", (result as GeminiResult.Success).output)
        verify(geminiApi).getTipsForHabit(eq("apiKey"), eq(chatMessages.toGeminiInputData()))
    }

    @Test
    fun getTipsForHabitImprovement_apiUnsuccessful_returnsErrorResult() = runTest {
        // 1. Create a real error body instead of mocking it
        val errorBody = "".toResponseBody(null)

        // 2. Pass the real object into Response.error
        whenever(geminiApi.getTipsForHabit(any(), any())).thenReturn(Response.error(404, errorBody))
        //whenever(geminiApi.getTipsForHabit(any(), any())).thenReturn(Response.error(404, mock()))

        val result = repository.getTipsForHabitImprovement("apiKey", emptyList())

        assertTrue(result is GeminiResult.Error)
        assertEquals("Sorry we can't help you with this", (result as GeminiResult.Error).message)
    }

    @Test
    fun getTipsForHabitImprovement_networkFailure_returnsNetworkErrorResult() = runTest {

        given(geminiApi.getTipsForHabit(any(), any())).willAnswer {
            throw IOException("No Internet")
        }

        //whenever(geminiApi.getTipsForHabit(any(), any())).thenThrow(IOException("No Internet"))

        val result = repository.getTipsForHabitImprovement("apiKey", emptyList())

        assertTrue(result is GeminiResult.Error)
        assertEquals("Network failure: Please check your connection", (result as GeminiResult.Error).message)
    }

    @Test
    fun getTipsForHabitImprovement_unexpectedException_returnsGenericErrorResult() = runTest {
        whenever(geminiApi.getTipsForHabit(any(), any())).thenThrow(RuntimeException("Unknown Error"))

        val result = repository.getTipsForHabitImprovement("apiKey", emptyList())

        assertTrue(result is GeminiResult.Error)
        assertTrue((result as GeminiResult.Error).message.contains("An unexpected error occurred"))
        assertTrue(result.message.contains("Unknown Error"))
    }

    // 4. Query & Flow Delegation

    @Test
    fun getAllHabitsWithLogs_returnsDaoFlow() = runTest {
        val expectedFlow = flowOf(emptyList<HabitWithLogs>())
        whenever(habitDao.getAllHabitsWithLogs()).thenReturn(expectedFlow)

        val result = repository.getAllHabitsWithLogs()

        assertEquals(expectedFlow, result)
        verify(habitDao).getAllHabitsWithLogs()
    }

    @Test
    fun getHabitWithLogsById_returnsDaoValue() = runTest {
        val habit = Habit(10L, "Exercise", Frequency.Daily, SyncState.SUCCESS, false, "remote_123", true, LocalTime.of(8, 0), "2024-01-01")
        val expectedValue = HabitWithLogs(habit, emptyList())
        whenever(habitDao.getHabitWithLogsById(10L)).thenReturn(expectedValue)

        val result = repository.getHabitWithLogsById(10L)

        assertEquals(expectedValue, result)
        verify(habitDao).getHabitWithLogsById(eq(10L))
    }

    @Test
    fun getCountOfLogsCompletedIn7Days_returnsDaoFlow() = runTest {
        val expectedFlow = flowOf(5L)
        whenever(habitDao.getCountOfLogsCompletedIn7Days(any(), any())).thenReturn(expectedFlow)

        val result = repository.getCountOfLogsCompletedIn7Days("today", "lastWeek")

        assertEquals(expectedFlow, result)
        verify(habitDao).getCountOfLogsCompletedIn7Days(eq("today"), eq("lastWeek"))
    }

    @Test
    fun getCountOfLogsCompletedInLast7Days_returnsDaoFlow() = runTest {
        val expectedFlow = flowOf(3L)
        whenever(habitDao.getCountOfLogsCompletedInLast7Days(any(), any())).thenReturn(expectedFlow)

        val result = repository.getCountOfLogsCompletedInLast7Days("lastWeek", "twoWeeksAgo")

        assertEquals(expectedFlow, result)
        verify(habitDao).getCountOfLogsCompletedInLast7Days(eq("lastWeek"), eq("twoWeeksAgo"))
    }

    @Test
    fun getTotalPossibleCompletionsInLast7Days_returnsDaoFlow() = runTest {
        val expectedFlow = flowOf(14L)
        whenever(habitDao.getTotalPossibleCompletionsInLast7Days(any())).thenReturn(expectedFlow)

        val result = repository.getTotalPossibleCompletionsInLast7Days("today")

        assertEquals(expectedFlow, result)
        verify(habitDao).getTotalPossibleCompletionsInLast7Days(eq("today"))
    }

    @Test
    fun getTotalPossibleCompletionsInLast7To14Days_returnsDaoFlow() = runTest {
        val expectedFlow = flowOf(10L)
        whenever(habitDao.getTotalPossibleCompletionsInLast7To14Days(any())).thenReturn(expectedFlow)

        val result = repository.getTotalPossibleCompletionsInLast7To14Days("lastWeek")

        assertEquals(expectedFlow, result)
        verify(habitDao).getTotalPossibleCompletionsInLast7To14Days(eq("lastWeek"))
    }

    @Test
    fun getAllHabitsWithLogsOrdered_returnsDaoFlow() = runTest {
        val expectedFlow = flowOf(emptyList<HabitWithLogs>())
        whenever(habitDao.getAllHabitsWithLogsOrdered()).thenReturn(expectedFlow)

        val result = repository.getAllHabitsWithLogsOrdered()

        assertEquals(expectedFlow, result)
        verify(habitDao).getAllHabitsWithLogsOrdered()
    }
}
