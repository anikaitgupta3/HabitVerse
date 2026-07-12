package com.anikaitgupta.habitverse.data.remote

import android.util.Log
import com.anikaitgupta.habitverse.data.Frequency
import com.anikaitgupta.habitverse.data.SyncState
import com.anikaitgupta.habitverse.data.db.Habit
import com.anikaitgupta.habitverse.data.db.HabitDao
import com.anikaitgupta.habitverse.domain.AuthRepository
import com.anikaitgupta.habitverse.toHabitDto
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.time.LocalTime

class SyncManagerImplTest {
    @Mock
    private lateinit var habitDao: HabitDao
    @Mock
    private lateinit var remoteDataSource: RemoteDataSource
    @Mock
    private lateinit var authRepository: AuthRepository

    private lateinit var syncManager: SyncManagerImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        syncManager = SyncManagerImpl(habitDao, remoteDataSource, authRepository)
        
        mockkStatic(Log::class)
        // Use aliased MockK any() to avoid conflict with Mockito any()
//        every { Log.v(any(), any()) } returns 0
//        every { Log.v(any(), any(), any()) } returns 0
//        every { Log.d(any(), any()) } returns 0
//        every { Log.d(any(), any(), any()) } returns 0
//        every { Log.i(mockkAny(), mockkAny()) } returns 0
//        every { Log.i(mockkAny(), mockkAny(), mockkAny()) } returns 0
//        every { Log.w(mockkAny<String>(), mockkAny<String>()) } returns 0
//        every { Log.w(mockkAny<String>(), mockkAny<Throwable>()) } returns 0
//        every { Log.w(mockkAny<String>(), mockkAny<String>(), mockkAny()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    private fun createHabit(
        id: Long = 1,
        remoteId: String? = null,
        isDeleted: Boolean = false,
        syncState: SyncState = SyncState.PENDING
    ) = Habit(
        id = id,
        habitName = "Exercise",
        habitFrequency = Frequency.Everyday,
        syncState = syncState,
        isDeleted = isDeleted,
        remoteId = remoteId,
        showNotification = false,
        timeToShowNotification = LocalTime.of(8, 0),
        createdAt = "2024-01-01"
    )

    @Test
    fun sync_whenNoPendingOrFailedHabits_doesNothing() = runTest {
        Mockito.`when`(habitDao.getAllPendingAndFailedHabits()).thenReturn(emptyList())
        syncManager.sync()
        Mockito.verifyNoInteractions(remoteDataSource)
        verify(habitDao, never()).editHabit(any())
    }

    @Test
    fun sync_whenHabitIsMarkedDeletedAndHasRemoteId_deletesRemotelyAndLocally() = runTest {
        val habit = createHabit(id = 1, remoteId = "remote_123", isDeleted = true)
        Mockito.`when`(habitDao.getAllPendingAndFailedHabits()).thenReturn(listOf(habit))
        Mockito.`when`(authRepository.getUserId()).thenReturn("user_1")

        syncManager.sync()

        verify(remoteDataSource).deleteHabit("remote_123", "user_1")
        verify(habitDao).deleteHabit(habit)
    }

    // 1. sync_whenHabitIsMarkedDeletedButHasNoRemoteId_onlyDeletesLocally
    @Test
    fun sync_whenHabitIsMarkedDeletedButHasNoRemoteId_onlyDeletesLocally() = runTest {
        val habit = createHabit(id = 1, remoteId = null, isDeleted = true)
        whenever(habitDao.getAllPendingAndFailedHabits()).thenReturn(listOf(habit))

        syncManager.sync()

        verify(remoteDataSource, never()).deleteHabit(anyString(), anyString())
        verify(habitDao).deleteHabit(habit)
    }

    // 2. sync_whenHabitIsNew_insertsRemotelyAndUpdatesLocalId
    @Test
    fun sync_whenHabitIsNew_insertsRemotelyAndUpdatesLocalId() = runTest {
        val habit = createHabit(id = 1, remoteId = null, isDeleted = false)
        whenever(habitDao.getAllPendingAndFailedHabits()).thenReturn(listOf(habit))
        whenever(authRepository.getUserId()).thenReturn("user_1")
        whenever(remoteDataSource.insertHabit(eq(habit.toHabitDto()), eq("user_1"))).thenReturn("new_remote_id")

        syncManager.sync()

        verify(remoteDataSource).insertHabit(any(), eq("user_1"))
        verify(habitDao).editHabit(habit.copy(remoteId = "new_remote_id", syncState = SyncState.SUCCESS))
    }

    // 3. sync_whenHabitExists_updatesRemotelyAndMarksSynced
    @Test
    fun sync_whenHabitExists_updatesRemotelyAndMarksSynced() = runTest {
        val habit = createHabit(id = 1, remoteId = "existing_id", isDeleted = false)
        whenever(habitDao.getAllPendingAndFailedHabits()).thenReturn(listOf(habit))
        whenever(authRepository.getUserId()).thenReturn("user_1")

        syncManager.sync()

        verify(remoteDataSource).updateHabit(eq(habit.toHabitDto()), eq("existing_id"), eq("user_1"))
        verify(habitDao).editHabit(habit.copy(syncState = SyncState.SUCCESS))
    }

    // 4. sync_whenRemoteCallFails_marksHabitAsFailedAndContinues
    @Test
    fun sync_whenRemoteCallFails_marksHabitAsFailedAndContinues() = runTest {
        val habit1 = createHabit(id = 1, remoteId = null, isDeleted = false)
        val habit2 = createHabit(id = 2, remoteId = "id2", isDeleted = false)
        whenever(habitDao.getAllPendingAndFailedHabits()).thenReturn(listOf(habit1, habit2))
        whenever(authRepository.getUserId()).thenReturn("user_1")
        
        whenever(remoteDataSource.insertHabit(any(), eq("user_1"))).thenThrow(RuntimeException("Network Error"))

        syncManager.sync()

        verify(habitDao).editHabit(habit1.copy(syncState = SyncState.FAILED))
        verify(remoteDataSource).updateHabit(any(), eq("id2"), eq("user_1"))
        verify(habitDao).editHabit(habit2.copy(syncState = SyncState.SUCCESS))
    }

    // 5. cleanRoomAndUpdateRoom_success_clearsLocalAndFetchesRemote
    @Test
    fun cleanRoomAndUpdateRoom_success_clearsLocalAndFetchesRemote() = runTest {
        whenever(authRepository.getUserId()).thenReturn("user_1")
        val habitDtos = listOf(
            HabitDto(id = 1, habitName = "H1"),
            HabitDto(id = 2, habitName = "H2")
        )
        whenever(remoteDataSource.getAllHabits("user_1")).thenReturn(habitDtos)

        syncManager.cleanRoomAndUpdateRoom()

        val inOrder = inOrder(habitDao, remoteDataSource)
        inOrder.verify(habitDao).deleteAllHabits()
        inOrder.verify(remoteDataSource).getAllHabits("user_1")
        verify(habitDao, times(2)).insertHabit(any())
    }

    // 6. cleanRoomAndUpdateRoom_remoteFetchFails_logsErrorAndDoesNotCrash
    @Test
    fun cleanRoomAndUpdateRoom_remoteFetchFails_logsErrorAndDoesNotCrash() = runTest {
        whenever(authRepository.getUserId()).thenReturn("user_1")
        whenever(remoteDataSource.getAllHabits("user_1")).thenThrow(RuntimeException("Remote Failure"))

        syncManager.cleanRoomAndUpdateRoom()

        verify(habitDao).deleteAllHabits()
        verify(habitDao, never()).insertHabit(any())
    }

    // 7. cleanRoom_callsDeleteAllHabits
    @Test
    fun cleanRoom_callsDeleteAllHabits() = runTest {
        syncManager.cleanRoom()
        verify(habitDao).deleteAllHabits()
    }

    // 8. deleteAccount_userLoggedIn_deletesRemoteDataAndClearsRoomReturnsSuccess
    @Test
    fun deleteAccount_userLoggedIn_deletesRemoteDataAndClearsRoomReturnsSuccess() = runTest {
        whenever(authRepository.getUserId()).thenReturn("user_123")

        val result = syncManager.deleteAccount()

        assertTrue(result.isSuccess)
        verify(remoteDataSource).deleteAccount("user_123")
        verify(habitDao).deleteAllHabits()
    }

    // 9. deleteAccount_userNotLoggedIn_doesNothingReturnsSuccess
    @Test
    fun deleteAccount_userNotLoggedIn_doesNothingReturnsSuccess() = runTest {
        whenever(authRepository.getUserId()).thenReturn(null)

        val result = syncManager.deleteAccount()

        assertTrue(result.isSuccess)
        verify(remoteDataSource, never()).deleteAccount(anyString())
        verify(habitDao, never()).deleteAllHabits()
    }

    // 10. deleteAccount_exceptionThrown_returnsFailureResult
    @Test
    fun deleteAccount_exceptionThrown_returnsFailureResult() = runTest {
        whenever(authRepository.getUserId()).thenReturn("user_123")
        val exception = RuntimeException("Deletion failed")
        whenever(remoteDataSource.deleteAccount("user_123")).thenThrow(exception)

        val result = syncManager.deleteAccount()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}