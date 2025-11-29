package com.example.smartfit

import com.example.smartfit.ui.screen.getSmartYAxisLabels
import org.junit.Assert.assertEquals
import org.junit.Test

class ChartLogicTest {

    @Test
    fun getSmartYAxisLabels_zeroInput_returnsDefaultScale() {
        val input = 0f
        val (maxVal, labels) = getSmartYAxisLabels(input)
        assertEquals(100f, maxVal, 0.01f)
        assertEquals(listOf("100", "66", "33", "0"), labels)
    }

    @Test
    fun getSmartYAxisLabels_calculatesNiceRoundNumbers() {
        val input = 85f

        val (maxVal, labels) = getSmartYAxisLabels(input)
        assertEquals(90f, maxVal, 0.01f)
        assertEquals(listOf("90", "60", "30", "0"), labels)
    }
}