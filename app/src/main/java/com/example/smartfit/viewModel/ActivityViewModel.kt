package com.example.smartfit.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartfit.SmartFitApplication
import com.example.smartfit.data.room.CalorieIntakeEntity
import com.example.smartfit.data.room.StepEntity
import com.example.smartfit.data.room.WorkoutEntity
import com.example.smartfit.repository.ActivityRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log

class ActivityViewModel(
    private val repository: ActivityRepository
) : ViewModel() {

    // --- Data Streams ---
    val allSteps: StateFlow<List<StepEntity>> = repository.allSteps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkouts: StateFlow<List<WorkoutEntity>> = repository.allWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFood: StateFlow<List<CalorieIntakeEntity>> = repository.allFood
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Actions ---

    // STEPS (Single Entry Logic)
    // This single function handles both ADD and UPDATE
    fun saveSteps(steps: Int, description: String) = viewModelScope.launch {
        Log.d("ActivityViewModel", "Attempting to save steps: $steps , Description: $description")
        repository.insertStep(getTodayDate(), steps, description)
        Log.d("ActivityViewModel", "Steps saved successfully for date: ${getTodayDate()}")
    }

    fun deleteStep(item: StepEntity) = viewModelScope.launch {
        repository.deleteStep(item)
    }

    // WORKOUTS
    fun addWorkout(type: String, duration: Int, description: String) = viewModelScope.launch {
        Log.d("ActivityViewModel", "Attempting to save workout: Type=$type , Duration=$duration , Desc=$description")
        repository.insertWorkout(getTodayDate(), type, duration, description)
        Log.d("ActivityViewModel", "Workout saved successfully")
    }

    // Explicit update for workouts (since multiple allowed per day)
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
        Log.d("ActivityViewModel", "Adding Food: $name , Qty: $qty , BaseCals: $baseCals")
        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        repository.insertFood(
            date = formattedDate,
            name = name,
            quantity = qty,
            unit = unit,
            caloriesPerUnit = baseCals,
            description = description,
            image = image
        )
        Log.d("ActivityViewModel", "Food item '$name' inserted into DB")
    }
    fun deleteFood(item: CalorieIntakeEntity) = viewModelScope.launch {
        repository.deleteFood(item)
    }

    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    class Factory(private val app: SmartFitApplication) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ActivityViewModel::class.java)) {
                return ActivityViewModel(app.repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}