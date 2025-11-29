package com.example.smartfit

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.smartfit.ui.screen.RegisterScreen
import org.junit.Rule
import org.junit.Test

class RegisterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()


    @Test
    fun register_emptyFields_showsErrorMessage() {
        composeTestRule.setContent {
            RegisterScreen(onRegisterSuccess = {}, onLoginClick = {})
        }

        composeTestRule.onNodeWithText("REGISTER").performClick()

        composeTestRule.onNodeWithText("Please fill all fields").assertIsDisplayed()
    }

    @Test
    fun register_passwordMismatch_showsError() {
        composeTestRule.setContent {
            RegisterScreen(onRegisterSuccess = {}, onLoginClick = {})
        }
        composeTestRule.onNodeWithText("Full Name").performTextInput("John Doe")
        composeTestRule.onNodeWithText("Email Address").performTextInput("john@test.com")
        composeTestRule.onNodeWithText("Password (8-20 chars)").performTextInput("12345678")
        composeTestRule.onNodeWithText("Confirm Password").performTextInput("87654321")

        composeTestRule.onNodeWithText("REGISTER").performClick()

        composeTestRule.onNodeWithText("Passwords do not match").assertIsDisplayed()
    }
}