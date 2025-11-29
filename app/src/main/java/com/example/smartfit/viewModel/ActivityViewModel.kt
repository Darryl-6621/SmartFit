package com.example.smartfit.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartfit.SmartFitApplication
import com.example.smartfit.data.dataStore.UserPreferences
import com.example.smartfit.data.room.CalorieIntakeEntity
import com.example.smartfit.data.room.StepEntity
import com.example.smartfit.data.room.WorkoutEntity
import com.example.smartfit.repository.ActivityRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityViewModel(
    private val repository: ActivityRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val allSteps: StateFlow<List<StepEntity>> = userPreferences.email
        .flatMapLatest { email ->
            if (!email.isNullOrEmpty()) {
                repository.getAllSteps(email)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val allWorkouts: StateFlow<List<WorkoutEntity>> = userPreferences.email
        .flatMapLatest { email ->
            if (!email.isNullOrEmpty()) {
                repository.getAllWorkouts(email)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val allFood: StateFlow<List<CalorieIntakeEntity>> = userPreferences.email
        .flatMapLatest { email ->
            if (!email.isNullOrEmpty()) {
                repository.getAllFood(email)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Actions ---

    // STEPS
    fun saveSteps(steps: Int, description: String) = viewModelScope.launch {
        val email = getCurrentUserEmail()
        if (email != null) {
            Log.d("ActivityViewModel", "Saving steps for user $email: $steps")
            repository.insertStep(getTodayDate(), steps, description, email)
        } else {
            Log.e("ActivityViewModel", "Cannot save steps: No user logged in")
        }
    }

    fun deleteStep(item: StepEntity) = viewModelScope.launch {
        repository.deleteStep(item)
    }

    // WORKOUTS
    fun addWorkout(type: String, duration: Int, description: String) = viewModelScope.launch {
        val email = getCurrentUserEmail()
        if (email != null) {
            Log.d("ActivityViewModel", "Saving workout for user $email")
            repository.insertWorkout(getTodayDate(), type, duration, description, email)
        }
    }

    fun updateWorkout(workout: WorkoutEntity) = viewModelScope.launch {
        repository.updateWorkout(workout)
    }

    fun deleteWorkout(item: WorkoutEntity) = viewModelScope.launch {
        repository.deleteWorkout(item)
    }

    // FOOD
    fun addFood(
        date: Date,
        name: String,
        qty: Double,
        unit: String,
        baseCals: Double,
        description: String,
        image: String?
    ) = viewModelScope.launch {
        val email = getCurrentUserEmail()
        if (email != null) {
            Log.d("ActivityViewModel", "Adding Food for $email: $name")
            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            repository.insertFood(
                date = formattedDate,
                name = name,
                quantity = qty,
                unit = unit,
                caloriesPerUnit = baseCals,
                description = description,
                image = image,
                email = email
            )
        }
    }

    fun deleteFood(item: CalorieIntakeEntity) = viewModelScope.launch {
        repository.deleteFood(item)
    }

    private suspend fun getCurrentUserEmail(): String? {
        val email = userPreferences.email.first()
        return if (email == "User") null else email
    }

    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    class Factory(private val app: SmartFitApplication) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ActivityViewModel::class.java)) {
                return ActivityViewModel(app.repository, UserPreferences(app)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}