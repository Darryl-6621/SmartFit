package com.example.smartfit.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartfit.SmartFitApplication
import com.example.smartfit.data.dataStore.GoalPreferences
import com.example.smartfit.data.dataStore.UserPreferences
import com.example.smartfit.data.room.CalorieIntakeEntity
import com.example.smartfit.data.room.StepEntity
import com.example.smartfit.data.room.User
import com.example.smartfit.data.room.WorkoutEntity
import com.example.smartfit.repository.ActivityRepository
import com.example.smartfit.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val allSteps: StateFlow<List<StepEntity>> = userPreferences.email
        .flatMapLatest { email ->
            if (isValidEmail(email)) {
                repository.getAllSteps(email!!)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val allWorkouts: StateFlow<List<WorkoutEntity>> = userPreferences.email
        .flatMapLatest { email ->
            if (isValidEmail(email)) {
                repository.getAllWorkouts(email!!)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val allFood: StateFlow<List<CalorieIntakeEntity>> = userPreferences.email
        .flatMapLatest { email ->
            if (isValidEmail(email)) {
                repository.getAllFood(email!!)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val stepCount: StateFlow<Int> = userPreferences.email
        .flatMapLatest { email ->
            if (isValidEmail(email)) {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                repository.getTodayStepCount(today, email!!)
            } else {
                flowOf(0)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)


    val stepGoal = goalPreferences.stepGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10000)
    val workoutGoal = goalPreferences.workoutGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 60)
    val calorieGoal = goalPreferences.calorieGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2000)



    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser: StateFlow<User?> = userPreferences.email
        .flatMapLatest { email ->
            if (isValidEmail(email)) userRepository.getUserFlow(email!!)
            else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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
    }

    private fun isValidEmail(email: String?): Boolean {
        return email != null && email != "User" && email.isNotEmpty()
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