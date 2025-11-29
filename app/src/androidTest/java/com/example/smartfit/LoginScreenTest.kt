package com.example.smartfit

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.smartfit.ui.screen.LoginScreen
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun login_withEmptyPassword_doesNotTriggerSuccess() {
        var loggedIn = false
        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = { loggedIn = true }, onRegisterClick = {})
        }
        composeTestRule.onNodeWithText("Email Address").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("LOGIN").performClick()
        assert(!loggedIn)
    }

    @Test
    fun login_passwordVisibility_toggles() {

        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = {}, onRegisterClick = {})
        }
        composeTestRule.onNodeWithText("Password").performTextInput("secret123")
        composeTestRule.onNodeWithContentDescription("Show password").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Show password").performClick()
        composeTestRule.onNodeWithContentDescription("Hide password").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Hide password").performClick()
        composeTestRule.onNodeWithContentDescription("Show password").assertIsDisplayed()
    }
}