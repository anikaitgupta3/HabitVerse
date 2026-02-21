package com.example.habitverse.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.habitverse.HabitVerseApp
import com.example.habitverse.data.Frequency
import com.example.habitverse.data.Habit
import com.example.habitverse.data.SyncManager
import com.example.habitverse.domain.HabitDomainModel
import com.example.habitverse.domain.HabitRepository
import com.example.habitverse.domain.HabitUseCase
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
    private val habitUseCase: HabitUseCase,private val syncManager: SyncManager
) : ViewModel() {

    private val _currentEditHabit = MutableStateFlow<HabitDomainModel?>(null)
//    private var currentSelectedFrequencyAddFragment: Frequency? = null
//    val _currentSelectedFrequencyAddFragment = currentSelectedFrequencyAddFragment
    private val _selectedFrequency = MutableStateFlow<Frequency?>(null)
    val selectedFrequency: StateFlow<Frequency?> = _selectedFrequency.asStateFlow()
    init {
        viewModelScope.launch(Dispatchers.IO) {
            syncManager.sync()
        }
    }

    val habitUiState: StateFlow<HabitUiState> =
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
        )

    fun addHabit(habit: HabitDomainModel) = viewModelScope.launch {
        habitUseCase.insertHabit(habit)
    }

    fun deleteHabit(habit: HabitDomainModel) = viewModelScope.launch {
        habitUseCase.deleteHabit(habit)
    }

    fun updateHabit(habit: HabitDomainModel) = viewModelScope.launch {
        habitUseCase.editHabit(habit)
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
    /*fun updateCurrentEditHabitById(id:Int){
        viewModelScope.launch {
            val habit=habitRepository.getHabitsById(id).first()
            _currentEditHabit.value=habit
        }
    }*/
}
