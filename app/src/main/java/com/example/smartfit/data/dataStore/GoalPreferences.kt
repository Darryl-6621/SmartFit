package com.example.smartfit.data.dataStore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GoalPreferences(private val context: Context) {

    companion object {
        private val STEP_GOAL_KEY = intPreferencesKey("step_goal")
        private val WORKOUT_GOAL_KEY = intPreferencesKey("workout_goal")
        private val CALORIE_GOAL_KEY = intPreferencesKey("calorie_goal")
    }

    val stepGoal: Flow<Int> = context.dataStore.data.map { it[STEP_GOAL_KEY] ?: 10000 }
    val workoutGoal: Flow<Int> = context.dataStore.data.map { it[WORKOUT_GOAL_KEY] ?: 60 }
    val calorieGoal: Flow<Int> = context.dataStore.data.map { it[CALORIE_GOAL_KEY] ?: 2000 }

    suspend fun saveGoals(steps: Int, workout: Int, calories: Int) {
        context.dataStore.edit { preferences ->
            preferences[STEP_GOAL_KEY] = steps
            preferences[WORKOUT_GOAL_KEY] = workout
            preferences[CALORIE_GOAL_KEY] = calories
        }
    }
}