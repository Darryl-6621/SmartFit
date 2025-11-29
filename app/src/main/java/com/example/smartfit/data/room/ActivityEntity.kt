package com.example.smartfit.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "steps")
data class StepEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val date: String,
    val steps: Int,
    val description: String = ""
)

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val date: String,
    val workoutType: String,
    val durationMinutes: Int,
    val description: String = ""
)

@Entity(tableName = "calorie_intake")
data class CalorieIntakeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val date: String,
    val foodName: String,
    val quantity: Double,
    val unit: String,
    val calories: Double,
    val description: String = "",
    val image: String? = null
)