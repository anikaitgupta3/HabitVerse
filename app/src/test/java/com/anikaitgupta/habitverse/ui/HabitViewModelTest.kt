package com.anikaitgupta.habitverse.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.WorkManager
import com.anikaitgupta.habitverse.data.Frequency
import com.anikaitgupta.habitverse.data.SyncState
import com.anikaitgupta.habitverse.data.db.Habit
import com.anikaitgupta.habitverse.data.db.HabitLog
import com.anikaitgupta.habitverse.data.db.HabitWithLogs
import com.anikaitgupta.habitverse.domain.AlarmScheduler
import com.anikaitgupta.habitverse.domain.AuthRepository
import com.anikaitgupta.habitverse.domain.HabitDomainModel
import com.anikaitgupta.habitverse.domain.HabitUseCase
import com.anikaitgupta.habitverse.domain.SyncManager
import com.anikaitgupta.habitverse.domain.AlarmItem
import com.anikaitgupta.habitverse.domain.ChatMessage
import com.anikaitgupta.habitverse.data.network.GeminiResult
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class HabitViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @Mock
    private lateinit var habitUseCase: HabitUseCase
    @Mock
    private lateinit var syncManager: SyncManager
    @Mock
    private lateinit var logSyncManager: SyncManager
    @Mock
    private lateinit var authRepository: AuthRepository
    @Mock
    private lateinit var workManager: WorkManager
    @Mock
    private lateinit var alarmScheduler: AlarmScheduler

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun init_whenUserIsLoggedIn_triggersInitialDataSynchronization()= runTest{
        Mockito.`when`(authRepository.checkLoggedIn()).thenReturn(true)
        HabitViewModel(habitUseCase,syncManager,logSyncManager,authRepository,workManager,alarmScheduler)
        advanceUntilIdle()
        verify(syncManager).sync()
        verify(logSyncManager).sync()
    }
    @Test
    fun init_whenUserIsNotLoggedIn_triggersInitialDataSynchronization()= runTest{
        Mockito.`when`(authRepository.checkLoggedIn()).thenReturn(false)
        HabitViewModel(habitUseCase,syncManager,logSyncManager,authRepository,workManager,alarmScheduler)
        advanceUntilIdle()
        verify(syncManager, never()).sync()
        verify(logSyncManager,never()).sync()
    }
    @Test
    fun habitUiState_whenLogsAreActive_mapsIsCompletedCorrectly()= runTest{
        val habit = Habit(
            1L,
            "Exercise",
            Frequency.Daily,
            SyncState.SUCCESS,
            false,
            "rid",
            true,
            LocalTime.NOON,
            "2024-01-01"
        )
        val logs = listOf(HabitLog(1L, 1L, "rid", LocalDate.now().toString(), SyncState.SUCCESS,false,"rid"))
        val habitWithLogs = HabitWithLogs(habit, logs)
        `when`(habitUseCase.getAllHabitsWithLogs()).thenReturn(flowOf(listOf(habitWithLogs)))
        `when`(authRepository.checkLoggedIn()).thenReturn(false)
        val viewModel = HabitViewModel(habitUseCase, syncManager, logSyncManager, authRepository, workManager, alarmScheduler)
        
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.habitUiState.collect()
        }
        
        advanceUntilIdle()
        assertEquals(true, viewModel.habitUiState.value.listOfHabits.firstOrNull()?.isCompleted)
    }
    @Test
    fun habitUiState_filtersOutDeletedLogs_marksHabitAsNotCompleted() = runTest {
        val habit = Habit(
            1L,
            "Exercise",
            Frequency.Daily,
            SyncState.SUCCESS,
            false,
            null,
            true,
            LocalTime.NOON,
            "2024-01-01"
        )
        val logs = listOf(HabitLog(1L, 1L, "2024-01-01", LocalDate.now().toString(), SyncState.SUCCESS,true,"rid"))
        val habitWithLogs = HabitWithLogs(habit, logs)
        `when`(habitUseCase.getAllHabitsWithLogs()).thenReturn(flowOf(listOf(habitWithLogs)))
        `when`(authRepository.checkLoggedIn()).thenReturn(false)
        val viewModel = HabitViewModel(habitUseCase, syncManager, logSyncManager, authRepository, workManager, alarmScheduler)
        
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.habitUiState.collect()
        }

        advanceUntilIdle()
        assertEquals(false, viewModel.habitUiState.value.listOfHabits.firstOrNull()?.isCompleted)
    }
    @Test
    fun habitUiState_whenCurrentEditHabitChanges_updatesUiState() = runTest {
        val habit = HabitDomainModel(
            1L,
            "Exercise",
            Frequency.Daily,
            "rid",
            false,
            LocalTime.NOON,
            false,
            "11-10-2024"
        )
        `when`(habitUseCase.getAllHabitsWithLogs()).thenReturn(flowOf(emptyList()))
        `when`(authRepository.checkLoggedIn()).thenReturn(false)
        val viewModel = HabitViewModel(habitUseCase, syncManager, logSyncManager, authRepository, workManager, alarmScheduler)
        
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.habitUiState.collect()
        }

        viewModel.updateCurrentEditHabit(habit)
        advanceUntilIdle()
        assertEquals(habit, viewModel.habitUiState.value.currentEditHabit)
    }

    @Test
    fun addHabit_insertsHabitAndSchedulesAlarm() = runTest {
        val sampleHabit = HabitDomainModel(
            id = 1L,
            habitName = "Exercise",
            habitFrequency = Frequency.Daily,
            remoteId = null,
            showNotification = true,
            timeToShowNotification = LocalTime.of(8, 0),
            isCompleted = false,
            createdAt = "2024-01-01"
        )
        `when`(habitUseCase.insertHabit(any())).thenReturn(42L)
        `when`(authRepository.checkLoggedIn()).thenReturn(false)
        val viewModel = HabitViewModel(habitUseCase, syncManager, logSyncManager, authRepository, workManager, alarmScheduler)

        viewModel.addHabit(sampleHabit)
        advanceUntilIdle()

        val inOrder = Mockito.inOrder(habitUseCase, alarmScheduler)
        inOrder.verify(habitUseCase).insertHabit(sampleHabit)
        inOrder.verify(alarmScheduler).schedule(AlarmItem(42L))
    }

    @Test
    fun deleteHabit_deletesHabitAndCancelsAlarm() = runTest {
        val sampleHabit = HabitDomainModel(
            id = 1L,
            habitName = "Exercise",
            habitFrequency = Frequency.Daily,
            remoteId = "rid",
            showNotification = true,
            timeToShowNotification = LocalTime.of(8, 0),
            isCompleted = false,
            createdAt = "2024-01-01"
        )
        `when`(authRepository.checkLoggedIn()).thenReturn(false)
        val viewModel = HabitViewModel(habitUseCase, syncManager, logSyncManager, authRepository, workManager, alarmScheduler)

        viewModel.deleteHabit(sampleHabit)
        advanceUntilIdle()

        verify(habitUseCase).deleteHabit(sampleHabit)
        verify(alarmScheduler).cancel(AlarmItem(1L))
    }

    @Test
    fun toggleCompletion_whenCurrentlyDone_deletesLog() = runTest {
        `when`(authRepository.checkLoggedIn()).thenReturn(false)
        val viewModel = HabitViewModel(habitUseCase, syncManager, logSyncManager, authRepository, workManager, alarmScheduler)
        val todayDate = LocalDate.now().toString()

        viewModel.toggleCompletion(habitId = 1L, isCurrentlyDone = true, remoteId = "rem_1")
        advanceUntilIdle()

        verify(habitUseCase).deleteLog(1L, todayDate)
    }

    @Test
    fun toggleCompletion_whenNotCurrentlyDone_insertsPendingLog() = runTest {
        `when`(authRepository.checkLoggedIn()).thenReturn(false)
        val viewModel = HabitViewModel(habitUseCase, syncManager, logSyncManager, authRepository, workManager, alarmScheduler)
        val todayDate = LocalDate.now().toString()

        viewModel.toggleCompletion(habitId = 1L, isCurrentlyDone = false, remoteId = "rem_1")
        advanceUntilIdle()

        verify(habitUseCase).insertLog(argThat {
            this.habitId == 1L && this.habitRemoteId == "rem_1" && this.completionDate == todayDate && this.syncState == SyncState.PENDING
        })
    }

    @Test
    fun login_success_updatesLoginStateToSuccess() = runTest {
        `when`(authRepository.checkLoggedIn()).thenReturn(false)
        val viewModel = HabitViewModel(habitUseCase, syncManager, logSyncManager, authRepository, workManager, alarmScheduler)

        viewModel.login("test@email.com", "password")
        advanceUntilIdle()

        assertEquals(LoginState.Success, viewModel.loginState.value)
    }

    @Test
    fun login_failure_emitsErrorState() = runTest {
        `when`(authRepository.checkLoggedIn()).thenReturn(false)
        val viewModel = HabitViewModel(habitUseCase, syncManager, logSyncManager, authRepository, workManager, alarmScheduler)
        Mockito.doAnswer { throw RuntimeException("Invalid Credentials") }
            .`when`(authRepository).signIn(Mockito.anyString(), Mockito.anyString())

        viewModel.login("test@email.com", "password")
        advanceUntilIdle()

        assertEquals(LoginState.Error("Invalid Credentials"), viewModel.loginState.value)
    }

    @Test
    fun deleteAccount_success_clearsAlarmsDataAndLogsOut() = runTest {
        `when`(authRepository.checkLoggedIn()).thenReturn(false)
        `when`(syncManager.deleteAccount()).thenReturn(Result.success(Unit))
        `when`(habitUseCase.getAllHabitsWithLogs()).thenReturn(flowOf(emptyList()))
        val viewModel = HabitViewModel(habitUseCase, syncManager, logSyncManager, authRepository, workManager, alarmScheduler)

        viewModel.deleteAccount()
        advanceUntilIdle()

        verify(alarmScheduler).cancelAll(Mockito.anyList())
        verify(authRepository).deleteAccount()
        verify(authRepository).logout()
        assertEquals(DeleteAccountState.Success, viewModel.deleteAccountState.value)
    }

    @Test
    fun addUserInput_updatesMessagesAndExposesLoadingState() = runTest {
        `when`(authRepository.checkLoggedIn()).thenReturn(false)
        `when`(habitUseCase.getTipsForHabitImprovement(Mockito.anyString(), Mockito.anyList())).thenReturn(GeminiResult.Loading)
        
        val viewModel = HabitViewModel(habitUseCase, syncManager, logSyncManager, authRepository, workManager, alarmScheduler)

        viewModel.addUserInput("How do I stay consistent?")
        advanceUntilIdle()
        
        assertTrue(viewModel.messages.value.any { it.role == "user" && it.text == "How do I stay consistent?" })
        assertEquals(GeminiUiState.Loading, viewModel.geminiUiState.value)
    }

    @Test
    fun getTipsForHabitImprovement_success_appendsModelResponse() = runTest {
        `when`(authRepository.checkLoggedIn()).thenReturn(false)
        `when`(habitUseCase.getTipsForHabitImprovement(Mockito.anyString(), Mockito.anyList()))
            .thenReturn(GeminiResult.Success("Keep trying!"))
        
        val viewModel = HabitViewModel(habitUseCase, syncManager, logSyncManager, authRepository, workManager, alarmScheduler)

        viewModel.getTipsForHabitImprovement()
        advanceUntilIdle()

        assertEquals(GeminiUiState.Success, viewModel.geminiUiState.value)
        assertTrue(viewModel.messages.value.any { it.role == "model" && it.text == "Keep trying!" })
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

}