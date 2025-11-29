package com.example.smartfit.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStep(step: StepEntity)

    @Update
    suspend fun updateStep(step: StepEntity)

    @Query("SELECT * FROM steps WHERE userEmail = :email ORDER BY date DESC")
    fun getAllSteps(email: String): Flow<List<StepEntity>>

    @Query("SELECT SUM(steps) FROM steps WHERE date = :todayDate AND userEmail = :email")
    fun getTodaySteps(todayDate: String, email: String): Flow<Int?>

    @Query("SELECT * FROM steps WHERE date = :date AND userEmail = :email LIMIT 1")
    suspend fun getStepByDate(date: String, email: String): StepEntity?

    @Delete
    suspend fun deleteStep(step: StepEntity)
}

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Query("SELECT * FROM workouts WHERE userEmail = :email ORDER BY date DESC")
    fun getAllWorkouts(email: String): Flow<List<WorkoutEntity>>

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)
}

@Dao
interface CalorieIntakeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: CalorieIntakeEntity)

    @Query("SELECT * FROM calorie_intake WHERE userEmail = :email ORDER BY date DESC")
    fun getAllFood(email: String): Flow<List<CalorieIntakeEntity>>

    @Delete
    suspend fun deleteFood(food: CalorieIntakeEntity)
}