package com.example.smartfit.repository

import com.example.smartfit.data.room.*
import com.example.smartfit.network.NinjasApiService
import com.example.smartfit.network.NinjasRetrofitInstance
import com.example.smartfit.network.SpoonacularApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ActivityRepository(
    private val stepDao: StepDao,
    private val workoutDao: WorkoutDao,
    private val calorieIntakeDao: CalorieIntakeDao,
    private val ninjaApi: NinjasApiService,
    private val spoonacularApi: SpoonacularApiService
) {
    val allSteps: Flow<List<StepEntity>> = stepDao.getAllSteps()
    val allWorkouts: Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()
    val allFood: Flow<List<CalorieIntakeEntity>> = calorieIntakeDao.getAllFood()

    fun getTodayStepCount(date: String): Flow<Int> = stepDao.getTodaySteps(date).map { it ?: 0 }

    suspend fun setTodaySteps(date: String, steps: Int) {
        insertStep(date, steps, "Manual Update")
    }

    suspend fun insertStep(date: String, steps: Int, description: String = "") {
        val existingStep = stepDao.getStepByDate(date)

        if (existingStep != null) {
            val updatedStep = existingStep.copy(
                steps = steps,
                description = description
            )
            stepDao.updateStep(updatedStep)
        } else {
            // Create new entry
            val newStep = StepEntity(
                date = date,
                steps = steps,
                description = description
            )
            stepDao.insertStep(newStep)
        }
    }

    suspend fun insertStepEntity(step: StepEntity) {
        stepDao.updateStep(step)
    }

    suspend fun updateStep(step: StepEntity) {
        stepDao.updateStep(step)
    }

    suspend fun insertWorkout(
        date: String,
        type: String,
        duration: Int,
        description: String
    ) {
        workoutDao.insertWorkout(
            WorkoutEntity(
                date = date,
                workoutType = type,
                durationMinutes = duration,
                description = description
            )
        )
    }

    suspend fun updateWorkout(workout: WorkoutEntity) {
        workoutDao.updateWorkout(workout)
    }

    suspend fun insertFood(
        date: String,
        name: String,
        quantity: Double,
        unit: String,
        caloriesPerUnit: Double,
        description: String,
        image: String? = null
    ) {
        // Repository calculates the total for storage
        val totalCalories = quantity * caloriesPerUnit

        calorieIntakeDao.insertFood(
            CalorieIntakeEntity(
                date = date,
                foodName = name,
                quantity = quantity,
                unit = unit,
                calories = totalCalories,
                description = description,
                image = image
            )
        )
    }
    suspend fun deleteStep(item: StepEntity) = stepDao.deleteStep(item)
    suspend fun deleteWorkout(item: WorkoutEntity) = workoutDao.deleteWorkout(item)
    suspend fun deleteFood(item: CalorieIntakeEntity) = calorieIntakeDao.deleteFood(item)

    data class WorkoutTip(
        val workoutName: String,
        val target: String,
        val summary: String
    )

    suspend fun getDailyQuote(): WorkoutTip {
        return try {
            val muscles = listOf(
                "abdominals", "biceps", "calves", "chest", "glutes",
                "hamstrings", "lats", "lower_back", "quadriceps", "triceps"
            )
            val randomMuscle = muscles.random()

            val exercises = NinjasRetrofitInstance.api.getExercises(muscle = randomMuscle)

            if (exercises.isNotEmpty()) {
                val ex = exercises.first()

                val sentences = ex.instructions.split(". ")
                val shortSummary =
                    if (sentences.size > 2) sentences.take(2).joinToString(". ") + "..."
                    else ex.instructions

                WorkoutTip(
                    workoutName = ex.name,
                    target = "${ex.muscle.uppercase()} (${ex.difficulty})",
                    summary = shortSummary
                )

            } else {
                WorkoutTip(
                    workoutName = "Workout Tip",
                    target = "General",
                    summary = "Stay consistent and keep moving!"
                )
            }

        } catch (_: Exception) {
            WorkoutTip(
                workoutName = "Workout Tip",
                target = "General",
                summary = "No internet? Try push-ups!"
            )
        }
    }

suspend fun searchFood(query: String, apiKey: String) =
        spoonacularApi.searchIngredients(query, apiKey)

    suspend fun getFoodDetails(id: Int, apiKey: String) =
        spoonacularApi.getIngredientInfo(id, apiKey)
}