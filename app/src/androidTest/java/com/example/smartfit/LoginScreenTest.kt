package com.example.smartfit

import androidx.compose.ui.test.junit4.createComposeRule
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
        // 1. 设置一个变量来检测是否登录成功
        var loggedIn = false

        // 2. 加载 LoginScreen
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = { loggedIn = true },
                onNavigateToRegister = {}
            )
        }

        // 3. 找到输入框并输入 Email (故意不输密码)
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")

        // 4. 点击登录按钮
        composeTestRule.onNodeWithText("LOGIN").performClick()

        // 5. 验证：loggedIn 应该依然是 false (因为密码为空)
        assert(!loggedIn)
    }
}