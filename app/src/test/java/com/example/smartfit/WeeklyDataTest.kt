package com.example.smartfit

import com.example.smartfit.data.room.CalorieIntakeEntity
import com.example.smartfit.data.room.StepEntity
import com.example.smartfit.data.room.WorkoutEntity
import com.example.smartfit.ui.screen.calculateWeeklyData
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeeklyDataTest {

    @Test
    fun calculateWeeklyData_returnsSevenDays() {
        val result = calculateWeeklyData(
            steps = emptyList<StepEntity>(),
            workouts = emptyList<WorkoutEntity>(),
            food = emptyList<CalorieIntakeEntity>()
        )

        assertEquals(7, result.size)
    }

    @Test
    fun calculateWeeklyData_correctlySumsSteps() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val mockSteps = listOf(
            StepEntity(
                date = today,
                steps = 1000,
                description = "",
                userEmail = "test@example.com"
            ),
            StepEntity(
                date = today,
                steps = 2000,
                description = "",
                userEmail = "test@example.com"
            )
        )

        val result = calculateWeeklyData(
            steps = mockSteps,
            workouts = emptyList<WorkoutEntity>(),
            food = emptyList<CalorieIntakeEntity>()
        )

        assertEquals(3000, result.last().steps)
    }
}