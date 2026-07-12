package com.anikaitgupta.habitverse.data.remote

import android.util.Log
import com.anikaitgupta.habitverse.data.SyncState
import com.anikaitgupta.habitverse.data.db.Habit
import com.anikaitgupta.habitverse.data.db.HabitDao
import com.anikaitgupta.habitverse.data.db.HabitLog
import com.anikaitgupta.habitverse.domain.AuthRepository
import com.anikaitgupta.habitverse.toHabitLogDto
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class LogSyncManagerImplTest {

    @Mock
    private lateinit var habitDao: HabitDao

    @Mock
    private lateinit var logRemoteDataSource: LogRemoteDataSource

    @Mock
    private lateinit var authRepository: AuthRepository

    private lateinit var logSyncManager: LogSyncManagerImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        logSyncManager = LogSyncManagerImpl(habitDao, logRemoteDataSource, authRepository)

        // Mock Android Log to avoid "Method e in android.util.Log not mocked"
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    private fun createLog(
        logId: Long = 1,
        habitId: Long = 101,
        remoteId: String? = null,
        habitRemoteId: String? = null,
        isDeleted: Boolean = false,
        syncState: SyncState = SyncState.PENDING
    ) = HabitLog(
        logId = logId,
        habitId = habitId,
        habitRemoteId = habitRemoteId,
        completionDate = "2024-01-01",
        syncState = syncState,
        isDeleted = isDeleted,
        remoteId = remoteId
    )

    // 1. sync() / syncSingleLog() Scenarios

    @Test
    fun sync_whenNoPendingOrFailedLogs_doesNothing() = runTest {
        whenever(habitDao.getPendingLogSyncs()).thenReturn(emptyList())

        logSyncManager.sync()

        verify(authRepository, never()).getUserId()
        verify(logRemoteDataSource, never()).insertLog(any(), any(), any())
        verify(habitDao, never()).updateLog(any())
    }

    @Test
    fun sync_whenUserNotLoggedIn_abortsImmediately() = runTest {
        val log = createLog()
        whenever(habitDao.getPendingLogSyncs()).thenReturn(listOf(log))
        whenever(authRepository.getUserId()).thenReturn(null)

        logSyncManager.sync()

        verify(logRemoteDataSource, never()).insertLog(any(), any(), any())
        verify(habitDao, never()).updateLog(any())
    }

    @Test
    fun sync_whenLogIsDeletedAndHasRemoteId_deletesRemotelyAndLocally() = runTest {
        val log = createLog(remoteId = "log_remote_123", isDeleted = true)
        whenever(habitDao.getPendingLogSyncs()).thenReturn(listOf(log))
        whenever(authRepository.getUserId()).thenReturn("user_abc")

        logSyncManager.sync()

        verify(logRemoteDataSource).deleteLog(eq("log_remote_123"), eq("user_abc"))
        verify(habitDao).permanentlyDeleteLog(log)
    }

    @Test
    fun sync_whenLogIsDeletedButHasNoRemoteId_onlyDeletesLocally() = runTest {
        val log = createLog(remoteId = null, isDeleted = true)
        whenever(habitDao.getPendingLogSyncs()).thenReturn(listOf(log))
        whenever(authRepository.getUserId()).thenReturn("user_abc")

        logSyncManager.sync()

        verify(logRemoteDataSource, never()).deleteLog(any(), any())
        verify(habitDao).permanentlyDeleteLog(log)
    }

    @Test
    fun sync_newLog_withExistingHabitRemoteId_insertsRemotelyDirectly() = runTest {
        val log = createLog(remoteId = null, habitRemoteId = "habit_remote_99")
        whenever(habitDao.getPendingLogSyncs()).thenReturn(listOf(log))
        whenever(authRepository.getUserId()).thenReturn("user_abc")
        whenever(logRemoteDataSource.insertLog(any(), any(), any())).thenReturn("new_log_id")

        logSyncManager.sync()

        verify(habitDao, never()).getHabitByIdSuspend(any())
        verify(logRemoteDataSource).insertLog(eq(log.toHabitLogDto()), eq(log.habitId), eq("user_abc"))
        verify(habitDao).updateLog(log.copy(remoteId = "new_log_id", syncState = SyncState.SUCCESS))
    }

    @Test
    fun sync_newLog_missingHabitRemoteIdButFoundInRoom_fetchesHabitIdUpdatesLocalAndInsertsRemotely() = runTest {
        val log = createLog(remoteId = null, habitRemoteId = null)
        val habit = Habit(id = 101, habitName = "Test", habitFrequency = com.anikaitgupta.habitverse.data.Frequency.Daily, syncState = SyncState.SUCCESS, isDeleted = false, remoteId = "habit_remote_room", showNotification = false, timeToShowNotification = java.time.LocalTime.NOON, createdAt = "2024")
        
        whenever(habitDao.getPendingLogSyncs()).thenReturn(listOf(log))
        whenever(authRepository.getUserId()).thenReturn("user_abc")
        whenever(habitDao.getHabitByIdSuspend(101L)).thenReturn(habit)
        whenever(logRemoteDataSource.insertLog(any(), any(), any())).thenReturn("new_log_id")

        logSyncManager.sync()

        val logWithHabitRemoteId = log.copy(habitRemoteId = "habit_remote_room")
        verify(habitDao).updateLog(logWithHabitRemoteId) // Initial update to add habitRemoteId
        verify(logRemoteDataSource).insertLog(any(), eq(101L), eq("user_abc"))
        verify(habitDao).updateLog(logWithHabitRemoteId.copy(remoteId = "new_log_id", syncState = SyncState.SUCCESS))
    }

    @Test
    fun sync_newLog_missingHabitRemoteIdAndNotFoundInRoom_insertsWithNullHabitRemoteId() = runTest {
        val log = createLog(remoteId = null, habitRemoteId = null)
        whenever(habitDao.getPendingLogSyncs()).thenReturn(listOf(log))
        whenever(authRepository.getUserId()).thenReturn("user_abc")
        whenever(habitDao.getHabitByIdSuspend(any())).thenReturn(null)
        whenever(logRemoteDataSource.insertLog(any(), any(), any())).thenReturn("new_log_id")

        logSyncManager.sync()

        verify(logRemoteDataSource).insertLog(eq(log.toHabitLogDto()), eq(101L), eq("user_abc"))
        verify(habitDao).updateLog(log.copy(remoteId = "new_log_id", syncState = SyncState.SUCCESS))
    }

    @Test
    fun sync_whenRemoteCallFails_marksLogAsFailedAndContinues() = runTest {
        val log1 = createLog(logId = 1)
        val log2 = createLog(logId = 2)
        whenever(habitDao.getPendingLogSyncs()).thenReturn(listOf(log1, log2))
        whenever(authRepository.getUserId()).thenReturn("user_abc")
        
        // Fail the first one
        whenever(logRemoteDataSource.insertLog(any(), any(), eq("user_abc")))
            .thenThrow(RuntimeException("Network Error"))
            .thenReturn("log2_remote_id")

        logSyncManager.sync()

        verify(habitDao).updateLog(log1.copy(syncState = SyncState.FAILED))
        verify(habitDao).updateLog(log2.copy(remoteId = "log2_remote_id", syncState = SyncState.SUCCESS))
    }

    // 2. cleanRoomAndUpdateRoom() Scenarios

    @Test
    fun cleanRoomAndUpdateRoom_success_resolvesHabitIdsAndInsertsLogs() = runTest {
        val logDto = HabitLogDto(logId = 5, habitRemoteId = "remote_h_1", completionDate = "2024-01-01", remoteId = "remote_l_1")
        val habit = Habit(id = 50, habitName = "H", habitFrequency = com.anikaitgupta.habitverse.data.Frequency.Daily, syncState = SyncState.SUCCESS, isDeleted = false, remoteId = "remote_h_1", showNotification = false, timeToShowNotification = java.time.LocalTime.NOON, createdAt = "2024")
        
        whenever(authRepository.getUserId()).thenReturn("user_abc")
        whenever(logRemoteDataSource.getAllLogs("user_abc")).thenReturn(listOf(logDto))
        whenever(habitDao.getHabitByRemoteId("remote_h_1")).thenReturn(habit)

        logSyncManager.cleanRoomAndUpdateRoom()

        verify(habitDao).deleteAllHabitLogs()
        verify(habitDao).insertLog(any())
    }

    @Test
    fun cleanRoomAndUpdateRoom_whenHabitRemoteIdCannotBeResolved_skipsLogInsertion() = runTest {
        val logDto = HabitLogDto(logId = 5, habitRemoteId = "unknown_h", completionDate = "2024-01-01", remoteId = "remote_l_1")
        
        whenever(authRepository.getUserId()).thenReturn("user_abc")
        whenever(logRemoteDataSource.getAllLogs("user_abc")).thenReturn(listOf(logDto))
        whenever(habitDao.getHabitByRemoteId("unknown_h")).thenReturn(null)

        logSyncManager.cleanRoomAndUpdateRoom()

        verify(habitDao).deleteAllHabitLogs()
        verify(habitDao, never()).insertLog(any())
    }

    @Test
    fun cleanRoomAndUpdateRoom_exceptionThrown_logsErrorAndDoesNotCrash() = runTest {
        whenever(authRepository.getUserId()).thenReturn("user_abc")
        whenever(logRemoteDataSource.getAllLogs(any())).thenThrow(RuntimeException("Fatal API Error"))

        logSyncManager.cleanRoomAndUpdateRoom()

        verify(habitDao).deleteAllHabitLogs()
        verify(habitDao, never()).insertLog(any())
        // Log.e check is handled by the mockk setup returning 0 and not crashing
    }

    // 3. Utility Methods

    @Test
    fun cleanRoom_callsDeleteAllHabitLogs() = runTest {
        logSyncManager.cleanRoom()
        verify(habitDao, times(1)).deleteAllHabitLogs()
    }

    @Test
    fun deleteAccount_returnsSuccessUnit() = runTest {
        val result = logSyncManager.deleteAccount()
        assertTrue(result.isSuccess)
    }
}
