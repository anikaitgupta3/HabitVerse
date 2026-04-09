package com.example.habitverse.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.habitverse.HabitVerseApp
import com.example.habitverse.NotificationUtils
import com.example.habitverse.data.Frequency
import com.example.habitverse.data.SyncState
import com.example.habitverse.data.db.Habit
import com.example.habitverse.data.db.HabitLog
import com.example.habitverse.di.HabitSync
import com.example.habitverse.di.LogSync
import com.example.habitverse.domain.AuthRepository
import com.example.habitverse.domain.HabitDomainModel
import com.example.habitverse.domain.HabitRepository
import com.example.habitverse.domain.HabitUseCase
import com.example.habitverse.domain.SyncManager
import com.example.habitverse.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class HabitUiState(
    val listOfHabits:List<HabitDomainModel> = listOf(),
    val currentEditHabit: HabitDomainModel? = null
)
/*class HabitViewModel(private val habitRepository: HabitRepository) : ViewModel() {
    // TODO: Implement the ViewModel
    private val _habitUiState = MutableStateFlow(HabitUiState())
    val habitUiState = _habitUiState

    fun getListOfHabits(){
        viewModelScope.launch {
            habitRepository.getAllHabits().collect { habits ->
                _habitUiState.update { it.copy(listOfHabits = habits)
                }
            }
        }
    }
    fun addHabit(habit: Habit){
        viewModelScope.launch {
            habitRepository.insertHabit(habit)
        }
    }
    fun deleteHabit(habit: Habit){
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
        }
    }
    fun updateHabit(habit: Habit){
        viewModelScope.launch {
            habitRepository.editHabit(habit)
        }
    }
    fun updateCurrentEditHabit(habit: Habit){
        _habitUiState.update { it.copy(currentEditHabit = habit)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as HabitVerseApp)
                val habitRepository = application.container.habitRepository
                HabitViewModel(habitRepository = habitRepository)
            }
        }
    }

}*/

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val habitUseCase: HabitUseCase, @HabitSync private val syncManager: SyncManager, @LogSync private val logSyncManager: SyncManager, private val authRepository: AuthRepository, private val workManager: WorkManager
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

//    private val _pickedTimeHour = MutableStateFlow(10)
//    val pickedTimeHour: StateFlow<Int> = _pickedTimeHour
//
//    private val _pickedTimeMinutes = MutableStateFlow(0)
//    val pickedTimeMinutes: StateFlow<Int> = _pickedTimeMinutes
    private var _pickedTimeHour: Int = 10
    val pickedTimeHour get() = _pickedTimeHour

    private var _pickedTimeMinutes: Int = 0
    val pickedTimeMinutes get() = _pickedTimeMinutes  // ❌ was _pickedTimeHour, fix this typo

    private val todayDate = LocalDate.now().toString()


    /*val habitUiState: StateFlow<HabitUiState> =
        combine(
            habitUseCase.getAllHabits(),
            _currentEditHabit
        ) { habits, currentEditHabit ->
            HabitUiState(
                listOfHabits = habits,
                currentEditHabit = currentEditHabit
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitUiState()
        )*/
    // Combined UI State
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

    fun addHabit(habit: HabitDomainModel) = viewModelScope.launch {
        val id = habitUseCase.insertHabit(habit)
        handleWorkManagement(habit.copy(id = id),isDeleted = false)

    }

    fun deleteHabit(habit: HabitDomainModel) = viewModelScope.launch {
        habitUseCase.deleteHabit(habit)
        handleWorkManagement(habit,isDeleted = true)
    }

    fun updateHabit(habit: HabitDomainModel) = viewModelScope.launch {
        habitUseCase.editHabit(habit)
        handleWorkManagement(habit,isDeleted = false)
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

         }
         viewModelScope.launch {
             logSyncManager.cleanRoomAndUpdateRoom()
             scheduleWorkManagerNotifications()


         }
    }
    fun scheduleWorkManagerNotifications(){
        val listOfHabits = habitUiState.value.listOfHabits
        listOfHabits.forEach {
            handleWorkManagement(it,isDeleted = false)
        }

    }
    fun cleanRoom(){
        viewModelScope.launch {
            syncManager.cleanRoom()
        }
        viewModelScope.launch {
            logSyncManager.cleanRoom()
        }
        workManager.cancelAllWork()
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
    /*fun updateCurrentEditHabitById(id:Int){
        viewModelScope.launch {
            val habit=habitRepository.getHabitsById(id).first()
            _currentEditHabit.value=habit
        }
    }*/
    // --- WorkManager Control Logic ---
    fun handleWorkManagement(habit: HabitDomainModel,isDeleted: Boolean) {
        if (habit.showNotification && !isDeleted) {
            val delay = NotificationUtils.calculateInitialDelay(habit.timeToShowNotification)
            val request = OneTimeWorkRequestBuilder<HabitReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MINUTES)
                .setInputData(workDataOf("HABIT_ID" to habit.id))
                .addTag("habit_${habit.id}")
                .build()

            workManager.enqueueUniqueWork(
                "habit_reminder_${habit.id}",
                ExistingWorkPolicy.REPLACE,
                request
            )
        } else {
            workManager.cancelUniqueWork("habit_reminder_${habit.id}")
        }
    }

    fun toggleCompletion(habitId: Long, isCurrentlyDone: Boolean) {
        viewModelScope.launch {
            if (isCurrentlyDone) habitUseCase.deleteLog(habitId, todayDate)
            else habitUseCase.insertLog(HabitLog(habitId = habitId, completionDate = todayDate, isDeleted = false, syncState = SyncState.PENDING, remoteId = null))
        }
    }
}

