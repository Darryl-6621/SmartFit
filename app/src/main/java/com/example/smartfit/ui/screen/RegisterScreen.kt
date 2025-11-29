package com.example.smartfit.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.smartfit.R
import com.example.smartfit.SmartFitApplication
import com.example.smartfit.data.room.User
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onLoginClick: () -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }
    var passwordMismatchError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val application = context.applicationContext as SmartFitApplication
    val userRepository = remember { application.userRepository }

    val darkPurple = Color(0xFF4A148C)
    val lightPurple = Color(0xFFB590F1)
    val surfaceWhite = Color.White
    val textOnPurple = Color.White
    val buttonPrimary = Color(0xFF4A148C)

    val gradientBrush = remember { Brush.verticalGradient(colors = listOf(lightPurple, darkPurple)) }

    Box(modifier = Modifier.fillMaxSize().background(gradientBrush)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.health),
                contentDescription = "App Logo",
                modifier = Modifier.size(110.dp).clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("SmartFit Register", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = textOnPurple)
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, "Person Icon") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, "Email Icon") },
                        isError = emailError,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (emailError) Text("Invalid email format or email already registered.", color = Color.Red, style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password (8-20 chars)") },
                        leadingIcon = { Icon(Icons.Default.Lock, "Lock Icon") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, "Toggle password")
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (passwordError) Text("Password must be between 8 and 20 characters", color = Color.Red, style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, "Lock Icon") },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, "Toggle password")
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (confirmPasswordError) Text("Confirm Password must be between 8 and 20 characters", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    if (passwordMismatchError) Text("Passwords do not match", color = Color.Red, style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            emailError = false; passwordError = false; confirmPasswordError = false; passwordMismatchError = false; errorMessage = null

                            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                                errorMessage = "Please fill all fields"; return@Button
                            }
                            if (!email.contains("@")) emailError = true
                            if (password.length !in 8..20) passwordError = true
                            if (confirmPassword.length !in 8..20) confirmPasswordError = true
                            if (!passwordError && !confirmPasswordError && password != confirmPassword) passwordMismatchError = true

                            if (!emailError && !passwordError && !confirmPasswordError && !passwordMismatchError) {
                                scope.launch {
                                    val newUser = User(fullName = fullName, email = email, password = password)
                                    val success = userRepository.registerUser(newUser)
                                    if (success) onRegisterSuccess() else { emailError = true; errorMessage = "Account with this email already exists." }
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonPrimary),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("REGISTER", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    if (errorMessage != null) Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 16.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onLoginClick) {
                val loginText = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = textOnPurple.copy(alpha = 0.8f))) { append("Already have an account? ") }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline, color = textOnPurple)) { append("Login") }
                }
                Text(text = loginText, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}