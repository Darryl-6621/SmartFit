package com.example.smartfit.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartfit.SmartFitApplication
import com.example.smartfit.data.room.CalorieIntakeEntity
import com.example.smartfit.data.room.StepEntity
import com.example.smartfit.data.room.User
import com.example.smartfit.data.room.WorkoutEntity
import com.example.smartfit.data.dataStore.GoalPreferences
import com.example.smartfit.data.dataStore.UserPreferences
import com.example.smartfit.repository.ActivityRepository
import com.example.smartfit.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel(
    private val repository: ActivityRepository,
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences,
    private val goalPreferences: GoalPreferences
) : ViewModel() {

    val allSteps: StateFlow<List<StepEntity>> = repository.allSteps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkouts: StateFlow<List<WorkoutEntity>> = repository.allWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFood: StateFlow<List<CalorieIntakeEntity>> = repository.allFood
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount.asStateFlow()

    val stepGoal = goalPreferences.stepGoal.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10000)
    val workoutGoal = goalPreferences.workoutGoal.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 60)
    val calorieGoal = goalPreferences.calorieGoal.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2000)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser: StateFlow<User?> = userPreferences.email
        .flatMapLatest { email ->
            if (email == null || email == "User") flowOf(null)
            else userRepository.getUserFlow(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // FIXED: Changed type from String to ActivityRepository.WorkoutTip
    private val _dailyQuote = MutableStateFlow(
        ActivityRepository.WorkoutTip(
            workoutName = "Loading...",
            target = "General",
            summary = "Fetching your daily tip..."
        )
    )
    val dailyQuote: StateFlow<ActivityRepository.WorkoutTip> = _dailyQuote.asStateFlow()

    init {
        viewModelScope.launch {
            _dailyQuote.value = repository.getDailyQuote()
        }

        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.getTodayStepCount(today).collect { total ->
                _stepCount.value = total
            }
        }
    }

    class Factory(private val app: SmartFitApplication) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(
                    app.repository,
                    app.userRepository,
                    UserPreferences(app),
                    GoalPreferences(app)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}