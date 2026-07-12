package com.anikaitgupta.habitverse.data.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.anikaitgupta.habitverse.data.Frequency
import com.anikaitgupta.habitverse.data.SyncState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalTime

class HabitDaoTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    lateinit var habitDatabase: HabitDatabase
    lateinit var habitDao: HabitDao

    @Before
    fun setUp() {
        habitDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HabitDatabase::class.java
        ).allowMainThreadQueries().build()
        habitDao = habitDatabase.habitDao()
    }

    @After
    fun tearDown() {
        habitDatabase.close()
    }

    private fun createHabit(
        id: Long = 0,
        name: String = "Test Habit",
        syncState: SyncState = SyncState.SUCCESS,
        isDeleted: Boolean = false,
        remoteId: String? = null,
        createdAt: String = "2024-01-01"
    ) = Habit(
        id = id,
        habitName = name,
        habitFrequency = Frequency.Everyday,
        syncState = syncState,
        isDeleted = isDeleted,
        remoteId = remoteId,
        showNotification = true,
        timeToShowNotification = LocalTime.of(10, 0),
        createdAt = createdAt
    )

    private fun createHabitLog(
        logId: Long = 0,
        habitId: Long,
        date: String = "2024-01-01",
        syncState: SyncState = SyncState.SUCCESS,
        isDeleted: Boolean = false,
        remoteId: String? = null
    ) = HabitLog(
        logId = logId,
        habitId = habitId,
        completionDate = date,
        syncState = syncState,
        isDeleted = isDeleted,
        remoteId = remoteId
    )

    // User's original tests
    @Test
    fun insertHabitGetSingleHabit() = runTest {
        val habit = Habit(1,"Eat good food", Frequency.Everyday, SyncState.SUCCESS,false,"abc",true,
            LocalTime.of(10,0),"abc")
        habitDao.insertHabit(habit)
        val result = habitDao.getAllHabitsAsList()
        assertEquals(1,result.size)
        assertEquals("Eat good food", result[0].habitName)
    }

    @Test
    fun insertHabitGetSingleHabitFlows() = runTest {
        val habit = Habit(1,"Eat good food", Frequency.Everyday, SyncState.SUCCESS,false,"abc",true,
            LocalTime.of(10,0),"abc")
        habitDao.insertHabit(habit)
        habitDao.getAllHabits().test{
            val habitList = awaitItem()
            assertEquals(1, habitList.size)
            cancel()
        }
    }

    @Test
    fun insertHabitGetSingleHabitFlows1() = runTest {
        val habit = Habit(1,"Eat good food", Frequency.Everyday, SyncState.SUCCESS,false,"abc",true,
            LocalTime.of(10,0),"abc")
        habitDao.insertHabit(habit)
        val result = habitDao.getAllHabits().first()
        assertEquals(1, result.size)
        assertEquals(habit.habitName, result[0].habitName)
    }

    // Generated tests based on requested scenarios
    @Test
    fun insertHabitAndDelete_returnsEmptyList() = runTest {
        val habit = createHabit(id = 1)
        habitDao.insertHabit(habit)
        habitDao.deleteHabit(habit)
        val result = habitDao.getAllHabitsAsList()
        assertTrue(result.isEmpty())
    }

    @Test
    fun insertAndEditHabit_updatesDatabase() = runTest {
        val habit = createHabit(id = 1, name = "Original")
        habitDao.insertHabit(habit)
        val updatedHabit = habit.copy(habitName = "Updated")
        habitDao.editHabit(updatedHabit)
        val result = habitDao.getAllHabitsAsList()
        assertEquals("Updated", result[0].habitName)
    }

    @Test
    fun insert3Habits_getHabitsById_returnsCorrectHabit() = runTest {
        habitDao.insertHabit(createHabit(id = 1))
        habitDao.insertHabit(createHabit(id = 2, name = "Target"))
        habitDao.insertHabit(createHabit(id = 3))
        
        habitDao.getHabitsById(2).test {
            val habit = awaitItem()
            assertEquals("Target", habit.habitName)
            cancel()
        }
    }

    @Test
    fun insert3Habits_getHabitByIdSuspend_returnsCorrectHabit() = runTest {
        habitDao.insertHabit(createHabit(id = 1))
        habitDao.insertHabit(createHabit(id = 2, name = "Target"))
        habitDao.insertHabit(createHabit(id = 3))
        
        val habit = habitDao.getHabitByIdSuspend(2)
        assertNotNull(habit)
        assertEquals("Target", habit?.habitName)
    }

    @Test
    fun insert4Habits_getAllPendingAndFailedHabits_returnsCorrectHabits() = runTest {
        habitDao.insertHabit(createHabit(id = 1, syncState = SyncState.SUCCESS))
        habitDao.insertHabit(createHabit(id = 2, syncState = SyncState.PENDING))
        habitDao.insertHabit(createHabit(id = 3, syncState = SyncState.FAILED))
        habitDao.insertHabit(createHabit(id = 4, syncState = SyncState.SUCCESS))
        
        val result = habitDao.getAllPendingAndFailedHabits()
        assertEquals(2, result.size)
        assertTrue(result.any { it.syncState == SyncState.PENDING })
        assertTrue(result.any { it.syncState == SyncState.FAILED })
    }

    @Test
    fun isUserTableNotEmpty_returnsCorrectBoolean() = runTest {
        assertFalse(habitDao.isUserTableNotEmpty())
        habitDao.insertHabit(createHabit())
        assertTrue(habitDao.isUserTableNotEmpty())
    }

    @Test
    fun deleteAllHabits_emptiesList() = runTest {
        habitDao.insertHabit(createHabit(id = 1))
        habitDao.insertHabit(createHabit(id = 2))
        habitDao.deleteAllHabits()
        val result = habitDao.getAllHabitsAsList()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getHabitByRemoteId_returnsCorrectHabitOrNull() = runTest {
        habitDao.insertHabit(createHabit(id = 1, remoteId = "rem_1"))
        
        val found = habitDao.getHabitByRemoteId("rem_1")
        assertEquals(1L, found?.id)
        
        val notFound = habitDao.getHabitByRemoteId("non_existent")
        assertNull(notFound)
    }

    @Test
    fun getAllHabitsWithLogs_returnsCorrectData() = runTest {
        val hId = habitDao.insertHabit(createHabit(id = 1))
        habitDao.insertLog(createHabitLog(habitId = hId, date = "2024-01-01"))
        habitDao.insertLog(createHabitLog(habitId = hId, date = "2024-01-02"))
        
        habitDao.getAllHabitsWithLogs().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals(2, list[0].logs.size)
            cancel()
        }
    }

    @Test
    fun habitLogOperations_returnsCorrectData() = runTest {
        val hId = habitDao.insertHabit(createHabit(id = 1, createdAt = "2024-01-01"))
        
        // getHabitWithLogsById
        habitDao.insertLog(createHabitLog(habitId = hId, date = "2024-01-01"))
        val habitWithLogs = habitDao.getHabitWithLogsById(hId)
        assertEquals(1, habitWithLogs?.logs?.size)
        
        // getPendingLogSyncs
        habitDao.insertLog(createHabitLog(habitId = hId, date = "2024-01-02", syncState = SyncState.PENDING))
        val pending = habitDao.getPendingLogSyncs()
        assertTrue(pending.any { it.syncState == SyncState.PENDING })
        
        // getCountOfLogsCompletedIn7Days
        habitDao.getCountOfLogsCompletedIn7Days("2024-01-07", "2024-01-01").test {
            assertEquals(2L, awaitItem())
            cancel()
        }

        // getCountOfLogsCompletedInLast7Days
        habitDao.insertLog(createHabitLog(habitId = hId, date = "2023-12-28"))
        habitDao.getCountOfLogsCompletedInLast7Days("2024-01-01", "2023-12-25").test {
            assertEquals(2L, awaitItem())
            cancel()
        }

        // markOldLogsAsDeleted
        val count = habitDao.markOldLogsAsDeleted("2024-01-02")
        assertEquals(2, count)
        
        // deleteAllHabitLogs
        habitDao.deleteAllHabitLogs()
        val emptyLogs = habitDao.getHabitWithLogsById(hId)?.logs
        assertTrue(emptyLogs.isNullOrEmpty())
    }

    @Test
    fun julianDayAnalytics_returnsCorrectCalculations() = runTest {
        habitDao.insertHabit(createHabit(id = 1, createdAt = "2024-01-01"))
        
        habitDao.getTotalPossibleCompletionsInLast7Days("2024-01-07").test {
            assertEquals(7L, awaitItem())
            cancel()
        }
        
        habitDao.getTotalPossibleCompletionsInLast7To14Days("2024-01-01").test {
            assertEquals(1L, awaitItem())
            cancel()
        }
    }

    @Test
    fun getAllHabitsWithLogsOrdered_returnsOrderedData() = runTest {
        habitDao.insertHabit(createHabit(id = 2))
        habitDao.insertHabit(createHabit(id = 1))
        habitDao.insertHabit(createHabit(id = 3))
        
        habitDao.getAllHabitsWithLogsOrdered().test {
            val list = awaitItem()
            assertEquals(3, list.size)
            assertEquals(1L, list[0].habit.id)
            assertEquals(2L, list[1].habit.id)
            assertEquals(3L, list[2].habit.id)
            cancel()
        }
    }
}
