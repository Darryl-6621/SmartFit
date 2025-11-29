package com.example.smartfit

import com.example.smartfit.util.CalorieCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class CalorieCalculatorTest {

    @Test
    fun calculate_running_isCorrect() {
        // Arrange (准备)
        val duration = 30
        val type = "Running"

        // Act (执行)
        val result = CalorieCalculator.calculate(type, duration)

        // Assert (验证)
        // 跑步 30分钟 * 10 = 300
        assertEquals(300, result)
    }

    @Test
    fun calculate_negativeDuration_returnsZero() {
        val result = CalorieCalculator.calculate("Running", -10)
        assertEquals(0, result)
    }
}