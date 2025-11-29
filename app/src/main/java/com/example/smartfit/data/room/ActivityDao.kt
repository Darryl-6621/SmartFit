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

    @Query("SELECT * FROM steps ORDER BY date DESC")
    fun getAllSteps(): Flow<List<StepEntity>>

    @Query("SELECT SUM(steps) FROM steps WHERE date = :todayDate")
    fun getTodaySteps(todayDate: String): Flow<Int?>

    // --- CRITICAL: Needed to check if today already has an entry ---
    @Query("SELECT * FROM steps WHERE date = :date LIMIT 1")
    suspend fun getStepByDate(date: String): StepEntity?

    @Delete
    suspend fun deleteStep(step: StepEntity)
}

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)
}

@Dao
interface CalorieIntakeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: CalorieIntakeEntity)

    @Query("SELECT * FROM calorie_intake ORDER BY date DESC")
    fun getAllFood(): Flow<List<CalorieIntakeEntity>>

    @Delete
    suspend fun deleteFood(food: CalorieIntakeEntity)
}