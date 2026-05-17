package com.anikaitgupta.habitverse.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.anikaitgupta.habitverse.BuildConfig
import com.anikaitgupta.habitverse.data.Frequency
import com.anikaitgupta.habitverse.data.SyncState
import com.anikaitgupta.habitverse.data.db.HabitLog
import com.anikaitgupta.habitverse.data.network.GeminiResult
import com.anikaitgupta.habitverse.di.HabitSync
import com.anikaitgupta.habitverse.di.LogSync
import com.anikaitgupta.habitverse.domain.AlarmItem
import com.anikaitgupta.habitverse.domain.AlarmScheduler
import com.anikaitgupta.habitverse.domain.AuthRepository
import com.anikaitgupta.habitverse.domain.HabitDomainModel
import com.anikaitgupta.habitverse.domain.HabitUseCase
import com.anikaitgupta.habitverse.domain.SyncManager
import com.anikaitgupta.habitverse.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HabitUiState(
    val listOfHabits:List<HabitDomainModel> = listOf(),
    val currentEditHabit: HabitDomainModel? = null
)
data class HabitAnalyticsUiState(
    //val logCountThisWeek: Long=0L,
    //val logCountLastWeek: Long=0L,
    //val habitCountThisWeek:Long=0L,
    //val habitCountLastWeek:Long=0L,
    val completionRateThisWeek:Double=0.0,
    val completionRateLastWeek: Double = 0.0,
    val trend: Double = 0.0,
    val recoveryRate: Double=0.0,
    val streakCount:Long =0L
)
sealed interface GeminiUiState {
    object Idle : GeminiUiState
    object Loading : GeminiUiState
    data class Success(val data: String) : GeminiUiState
    data class Error(val message: String) : GeminiUiState
}

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val habitUseCase: HabitUseCase, @HabitSync private val syncManager: SyncManager, @LogSync private val logSyncManager: SyncManager, private val authRepository: AuthRepository, private val workManager: WorkManager,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _currentEditHabit = MutableStateFlow<HabitDomainModel?>(null)
//    private var currentSelectedFrequencyAddFragment: Frequency? = null
//    val _currentSelectedFrequencyAddFragment = currentSelectedFrequencyAddFragment
    private val _selectedFrequency = MutableStateFlow<Frequency?>(null)
    val selectedFrequency: StateFlow<Frequency?> = _selectedFrequency.asStateFlow()
    init {
        if(checkLoggedIn()) {
            viewModelScope.launch(Dispatchers.IO) {
                syncManager.sync()
            }
            viewModelScope.launch(Dispatchers.IO) {
                logSyncManager.sync()
            }
        }
    }
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState = _registrationState.asStateFlow()
    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState = _forgotPasswordState.asStateFlow()

    private val _deleteAccountState = MutableStateFlow<DeleteAccountState>(DeleteAccountState.Idle)
    val deleteAccountState = _deleteAccountState.asStateFlow()
    private val _geminiUiState = MutableStateFlow<GeminiUiState>(GeminiUiState.Idle)
    val geminiUiState = _geminiUiState.asStateFlow()

    private var _pickedTimeHour: Int = 10
    val pickedTimeHour get() = _pickedTimeHour

    private var _pickedTimeMinutes: Int = 0
    val pickedTimeMinutes get() = _pickedTimeMinutes  // ❌ was _pickedTimeHour, fix this

    private var tipsJob: Job? = null


    private val todayDate = LocalDate.now().toString()
    val habitUiState: StateFlow<HabitUiState> = combine(
        habitUseCase.getAllHabitsWithLogs(),
        _currentEditHabit
    ) { habitsWithLogs, currentEdit ->
        val mapped = habitsWithLogs.map { item ->
           // item.habit.toDomain(isCompleted = item.logs.any { it.completionDate == todayDate })
            // CRITICAL: Filter out logs that are marked as deleted locally
            val activeLogs = item.logs.filter { !it.isDeleted }

            val isDone = activeLogs.any { it.completionDate == todayDate }
            item.habit.toDomain(isCompleted = isDone)
        }
        HabitUiState(listOfHabits = mapped, currentEditHabit = currentEdit)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HabitUiState())

//    val analyticsUiState:StateFlow<HabitAnalyticsUiState> = combine(
//        habitUseCase.getCountOfLogsCompletedIn7Days(LocalDate.now().toString(), LocalDate.now().minusDays(6).toString()),
//        habitUseCase.getCountOfLogsCompletedInLast7Days(LocalDate.now().minusDays(7).toString(), LocalDate.now().minusDays(13).toString()),
//        habitUseCase.getTotalPossibleCompletionsInLast7Days(LocalDate.now().toString()),
//        habitUseCase.getTotalPossibleCompletionsInLast7To14Days(LocalDate.now().minusDays(7).toString()),
//        habitUseCase.getRecoveryRate(),
//        habitUseCase.calculateNumberOfHabitsWithStreak()
//    ){logsCurr,logsPrev,totalCurr,totalPrev,recoveryRate,streakCount->
//        HabitAnalyticsUiState(logCountThisWeek = logsCurr, logCountLastWeek = logsPrev, habitCountThisWeek = totalCurr, habitCountLastWeek = totalPrev, recoveryRate = recoveryRate, streakCount = streakCount)
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HabitAnalyticsUiState())
//val analyticsUiState: StateFlow<HabitAnalyticsUiState> = combine(
//    combine(
//        habitUseCase.getCountOfLogsCompletedIn7Days(
//            LocalDate.now().toString(),
//            LocalDate.now().minusDays(6).toString()
//        ),
//        habitUseCase.getCountOfLogsCompletedInLast7Days(
//            LocalDate.now().minusDays(7).toString(),
//            LocalDate.now().minusDays(13).toString()
//        ),
//        habitUseCase.getTotalPossibleCompletionsInLast7Days(
//            LocalDate.now().toString()
//        )
//    ) { logsCurr, logsPrev, totalCurr ->
//        Triple(logsCurr, logsPrev, totalCurr)
//    },
//    combine(
//        habitUseCase.getTotalPossibleCompletionsInLast7To14Days(
//            LocalDate.now().minusDays(7).toString()
//        ),
//        habitUseCase.getRecoveryRate(),
//        habitUseCase.calculateNumberOfHabitsWithStreak()
//    ) { totalPrev, recoveryRate, streakCount ->
//        Triple(totalPrev, recoveryRate, streakCount)
//    }
//) { (logsCurr, logsPrev, totalCurr), (totalPrev, recoveryRate, streakCount) ->
//    HabitAnalyticsUiState(
//        logCountThisWeek = logsCurr,
//        logCountLastWeek = logsPrev,
//        habitCountThisWeek = totalCurr,
//        habitCountLastWeek = totalPrev,
//        recoveryRate = recoveryRate,
//        streakCount = streakCount
//    )
//}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HabitAnalyticsUiState())

    val analyticsUiState: StateFlow<HabitAnalyticsUiState> = combine(
        habitUseCase.getAnalyticsData(),
        habitUseCase.getRecoveryRate(),
        habitUseCase.calculateNumberOfHabitsWithStreak()
    ) { analyticsData, recoveryRate, streakCount ->
        HabitAnalyticsUiState(
//            logCountThisWeek = analyticsData.logCountThisWeek,
//            logCountLastWeek = analyticsData.logCountLastWeek,
//            habitCountThisWeek = analyticsData.totalPossibleThisWeek,
//            habitCountLastWeek = analyticsData.totalPossibleLastWeek,
            completionRateThisWeek = analyticsData.completionRateThisWeek,
            completionRateLastWeek = analyticsData.completionRateLastWeek,
            trend = analyticsData.trend,
            recoveryRate = recoveryRate,
            streakCount = streakCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HabitAnalyticsUiState())

    fun addHabit(habit: HabitDomainModel) = viewModelScope.launch {
        val id = habitUseCase.insertHabit(habit)
        //handleWorkManagement(habit.copy(id = id),isDeleted = false)
        handleAlarmManagement(habit.copy(id = id),isDeleted = false)

    }

    fun deleteHabit(habit: HabitDomainModel) = viewModelScope.launch {
        habitUseCase.deleteHabit(habit)
        //handleWorkManagement(habit,isDeleted = true)
        handleAlarmManagement(habit,isDeleted = true)
    }

    fun updateHabit(habit: HabitDomainModel) = viewModelScope.launch {
        habitUseCase.editHabit(habit)
        //handleWorkManagement(habit,isDeleted = false)
        handleAlarmManagement(habit,isDeleted = false)
    }
    fun updateCurrentFrequencyFragment(frequency: Frequency){
        _selectedFrequency.value = frequency
    }
    fun updateCurrentFrequencyFragmentToNull(){
        _selectedFrequency.value = null
    }

    fun updateCurrentEditHabit(habit: HabitDomainModel?) {
        _currentEditHabit.value = habit
    }
    fun syncAllPendingAndFailedHabits(){
        viewModelScope.launch {
            syncManager.sync()
        }
        viewModelScope.launch {
            logSyncManager.sync()
        }
    }
     fun clearRoomAndUpdateRoom(){
         viewModelScope.launch {
             syncManager.cleanRoomAndUpdateRoom()
             scheduleNotifications()

         }
         viewModelScope.launch {
             logSyncManager.cleanRoomAndUpdateRoom()
         }

    }
    suspend fun scheduleNotifications(){
        //val listOfHabits = habitUiState.value.listOfHabits
        val listOfHabits = habitUseCase.getAllHabitsWithLogs().first().map { it.habit }
        listOfHabits.forEach {
            handleAlarmManagement(it.toDomain(false),isDeleted = false)
        }

    }
    fun cleanRoom(){
        viewModelScope.launch {
            syncManager.cleanRoom()
        }
        viewModelScope.launch {
            logSyncManager.cleanRoom()
        }
        //workManager.cancelAllWork()
        deleteAllAlarms()
    }
     fun createAccount(emailId: String, password: String) {
        viewModelScope.launch {
            try {
                authRepository.createAccount(emailId, password)
                _registrationState.value = RegistrationState.Success
            } catch (e: Exception) {
                _registrationState.value =
                    RegistrationState.Error(e.message ?: "Error in creating account")
            }
        }

    }
     fun login(emailId: String, password: String) {
         viewModelScope.launch {
             try {
                 authRepository.signIn(emailId, password)
                 _loginState.value = LoginState.Success
             } catch (e: Exception) {
                 _loginState.value = LoginState.Error(e.message ?: "Error in creating login")
             }
         }
    }
    fun updateLoginStateToIdle(){
        _loginState.value = LoginState.Idle
    }
    fun updateRegistrationStateToIdle(){
        _registrationState.value = RegistrationState.Idle
    }
    fun logout(){
      viewModelScope.launch {
          authRepository.logout()
      }
    }
    fun checkLoggedIn(): Boolean{
        return authRepository.checkLoggedIn()
    }

    fun setPickedTime(hour: Int, minutes: Int) {
        _pickedTimeHour = hour
        _pickedTimeMinutes = minutes
    }

    fun handleAlarmManagement(habit: HabitDomainModel,isDeleted: Boolean) {
        if (habit.showNotification && !isDeleted) {
            viewModelScope.launch {
                alarmScheduler.schedule(AlarmItem(habit.id!!))
            }
        } else {
            viewModelScope.launch {
                alarmScheduler.cancel(AlarmItem(habit.id!!))
            }
        }
    }
    fun deleteAllAlarms(){
        viewModelScope.launch {
            alarmScheduler.cancelAll(habitUiState.value.listOfHabits.map { AlarmItem(it.id!!) })
        }
    }

    fun getTipsForHabitImprovement(habitName: String) {
        tipsJob?.cancel()
        tipsJob = viewModelScope.launch {
            _geminiUiState.value = GeminiUiState.Loading
            val result = habitUseCase.getTipsForHabitImprovement(BuildConfig.API_KEY, habitName)
            _geminiUiState.value = when (result) {
                is GeminiResult.Success -> GeminiUiState.Success(result.output)
                is GeminiResult.Error -> GeminiUiState.Error(result.message)
                is GeminiResult.Loading -> GeminiUiState.Loading
            }
        }
    }
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            try {
                authRepository.sendPasswordResetMail(email)
                _forgotPasswordState.value = ForgotPasswordState.Success

            } catch (e: Exception) {
                _forgotPasswordState.value =
                    ForgotPasswordState.Error(e.message ?: "Error sending reset email")
            }
        }
    }

    fun updateForgotPasswordStateToIdle() {
        _forgotPasswordState.value = ForgotPasswordState.Idle
    }

    fun resetGeminiState() {
        _geminiUiState.value = GeminiUiState.Idle
    }
    fun deleteAccount(){
        viewModelScope.launch {
            try {
                val result = syncManager.deleteAccount()
                if (result.isSuccess) {
                    authRepository.deleteAccount()
                    authRepository.logout()
                    _deleteAccountState.value = DeleteAccountState.Success
                } else {
                    _deleteAccountState.value = DeleteAccountState.Error(result.exceptionOrNull()?.message ?: "Failed to delete account data")
                }
            } catch (e: Exception) {
                _deleteAccountState.value = DeleteAccountState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun updateDeleteAccountStateToIdle() {
        _deleteAccountState.value = DeleteAccountState.Idle
    }

    fun toggleCompletion(habitId: Long, isCurrentlyDone: Boolean) {
        viewModelScope.launch {
            if (isCurrentlyDone) habitUseCase.deleteLog(habitId, todayDate)
            else habitUseCase.insertLog(HabitLog(habitId = habitId, completionDate = todayDate, isDeleted = false, syncState = SyncState.PENDING, remoteId = null))
        }
    }
}

