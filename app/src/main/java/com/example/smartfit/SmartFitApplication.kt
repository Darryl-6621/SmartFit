package com.example.smartfit

import android.app.Application
import com.example.smartfit.data.room.AppDatabase
import com.example.smartfit.network.NinjasRetrofitInstance
import com.example.smartfit.network.SpoonacularInstance
import com.example.smartfit.repository.ActivityRepository
import com.example.smartfit.repository.UserRepository

class SmartFitApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }

    val repository by lazy {
        ActivityRepository(
            stepDao = database.stepDao(),
            workoutDao = database.workoutDao(),
            calorieIntakeDao = database.calorieIntakeDao(),
            ninjaApi = NinjasRetrofitInstance.api,
            spoonacularApi = SpoonacularInstance.api
        )
    }

    val userRepository by lazy {
        UserRepository(database.userDao())
    }
}