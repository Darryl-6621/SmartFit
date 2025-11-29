package com.example.smartfit.ui.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.smartfit.data.dataStore.UserPreferences
import com.example.smartfit.data.dataStore.GoalPreferences
import com.example.smartfit.SmartFitApplication
import com.example.smartfit.data.room.User
import com.example.smartfit.repository.UserRepository
import kotlinx.coroutines.launch

private val LogoutRed = Color(0xFFD32F2F)

data class ProfileColors(val background: Color, val surface: Color, val primary: Color, val textPrimary: Color, val textSecondary: Color, val border: Color)

@Composable
fun ProfileScreen(navController: NavController, onLogoutClick: () -> Unit) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val goalPreferences = remember { GoalPreferences(context) }
    val scope = rememberCoroutineScope()

    val userEmail by userPreferences.email.collectAsState(initial = "Loading...")
    val isDarkMode by userPreferences.isDarkMode.collectAsState(initial = false)

    val stepGoal by goalPreferences.stepGoal.collectAsState(initial = 10000)
    val workoutGoal by goalPreferences.workoutGoal.collectAsState(initial = 60)
    val calorieGoal by goalPreferences.calorieGoal.collectAsState(initial = 2000)

    val application = context.applicationContext as SmartFitApplication
    val userRepository = remember { application.userRepository }

    // --- FIXED: Dynamic Colors based on Dark Mode ---
    val colors = if (isDarkMode) {
        ProfileColors(
            background = Color(0xFF121212),
            surface = Color(0xFF424242), // Dark Grey for Cards (Visible in Dark Mode)
            primary = Color(0xFFD0BCFF),
            textPrimary = Color.White,
            textSecondary = Color(0xFFE0E0E0),
            border = Color(0xFF505050)
        )
    } else {
        ProfileColors(
            background = Color(0xFFF9FAFB),
            surface = Color.White,
            primary = Color(0xFF4A148C),
            textPrimary = Color(0xFF1A1C1E),
            textSecondary = Color(0xFF596068),
            border = Color(0xFFE0E0E0)
        )
    }

    var refreshKey by remember { mutableIntStateOf(0) }
    val fullUserDetails: User? by produceState<User?>(initialValue = null, userEmail, refreshKey) {
        if (userEmail != "Loading..." && userEmail != "User") value = userRepository.getUserByEmail(userEmail!!)
    }

    if (userEmail != "Loading..." && userEmail != "User" && fullUserDetails == null) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = onLogoutClick) { Text("Reset Session") }
        }
        return
    }

    val currentFullName = fullUserDetails?.fullName ?: "SmartFit User"
    val dbImageUri = fullUserDetails?.profileImageUri

    var showUserDetailsDialog by remember { mutableStateOf(false) }
    var showGoalsDialog by remember { mutableStateOf(false) }

    Scaffold(containerColor = colors.background) { paddingValues ->
        LazyColumn(Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
            item {
                Spacer(Modifier.height(20.dp))
                Text("Profile", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            }
            item {
                ProfileImageWithUploader(dbImageUri, colors) { uri ->
                    scope.launch {
                        fullUserDetails?.let { userRepository.updateUser(it.copy(profileImageUri = uri.toString())); refreshKey++ }
                    }
                }
            }
            item {
                ModernInfoCard("Details", Icons.Default.Person, colors, { showUserDetailsDialog = true }) {
                    InfoRow("Name", currentFullName, colors)
                    Divider(color = colors.border.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 12.dp)) // Added separator
                    InfoRow("Email", userEmail?:"", colors)
                }
            }
            item {
                ModernInfoCard("Goals", Icons.AutoMirrored.Filled.DirectionsRun, colors, { showGoalsDialog = true }) {
                    GoalRowModern("Steps", "$stepGoal", Icons.AutoMirrored.Filled.DirectionsWalk, colors)
                    GoalRowModern("Workout", "$workoutGoal min", Icons.Filled.FitnessCenter, colors)
                    GoalRowModern("Calories", "$calorieGoal kcal", Icons.Default.LocalFireDepartment, colors)
                }
            }
            item {
                ModernSettingsCard(isDarkMode, colors) { scope.launch { userPreferences.saveThemeMode(it) } }
            }
            item {
                Button(
                    onClick = onLogoutClick,
                    colors = ButtonDefaults.buttonColors(containerColor = LogoutRed),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    // FIXED: Explicit White Color for Text
                    Text("Log Out", color = Color.White)
                }
            }
        }
    }

    if (showUserDetailsDialog) EditUserDetailsDialog(fullUserDetails, colors, { showUserDetailsDialog = false }) {
        scope.launch { userRepository.updateUser(it); userPreferences.saveEmail(it.email); refreshKey++; showUserDetailsDialog = false }
    }

    if (showGoalsDialog) EditDailyTargetsDialog(stepGoal, workoutGoal, calorieGoal, colors, { showGoalsDialog = false }) { s, w, c ->
        scope.launch { goalPreferences.saveGoals(s, w, c); showGoalsDialog = false }
    }
}

@Composable
fun ModernInfoCard(title: String, icon: ImageVector, colors: ProfileColors, onEditClick: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = colors.surface), border = BorderStroke(1.dp, colors.border), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(colors.primary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = colors.primary, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
                Button(onClick = onEditClick, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.primary.copy(alpha = 0.15f), contentColor = colors.primary), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), elevation = ButtonDefaults.buttonElevation(0.dp), modifier = Modifier.height(36.dp)) {
                    Text("Edit", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
            Divider(modifier = Modifier.padding(vertical = 16.dp), color = colors.border)
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, colors: ProfileColors) {
    // FIXED: Added Vertical padding for spacing and increased spacer height
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary.copy(alpha = 0.8f))
        Spacer(Modifier.height(8.dp)) // Increased spacing
        Text(value, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun GoalRowModern(label: String, value: String, icon: ImageVector, colors: ProfileColors) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = colors.primary)
    }
}

@Composable
fun ModernSettingsCard(isDarkMode: Boolean, colors: ProfileColors, onToggle: (Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = colors.surface), border = BorderStroke(1.dp, colors.border), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon visibility fixed by dynamic colors.surface (Dark Grey background) vs Icon White
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(if (isDarkMode) Color.White.copy(alpha=0.1f) else Color.Yellow.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Icon(if (isDarkMode) Icons.Default.DarkMode else Icons.Default.WbSunny, null, tint = if (isDarkMode) Color.White else Color.Black, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Dark Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Text(if (isDarkMode) "On" else "Off", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                }
            }
            Switch(checked = isDarkMode, onCheckedChange = onToggle, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.primary))
        }
    }
}

@Composable
fun ProfileImageWithUploader(imageUri: String?, colors: ProfileColors, onImageSelected: (Uri?) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (e: Exception) {}
            onImageSelected(uri)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.size(120.dp).clip(RoundedCornerShape(24.dp)).border(2.dp, colors.border, RoundedCornerShape(24.dp)).background(if(colors.background == Color(0xFF121212)) Color(0xFF2C2C2C) else Color(0xFFEEEEEE)),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(rememberAsyncImagePainter(Uri.parse(imageUri)), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Default.AccountCircle, null, Modifier.size(64.dp), tint = colors.textSecondary)
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { launcher.launch("image/*") }, colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.White), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp), elevation = ButtonDefaults.buttonElevation(0.dp)) {
            Icon(Icons.Default.CameraAlt, null, Modifier.size(18.dp), tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Upload Photo", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
        }
    }
}

// --- DIALOGS ---
@Composable
fun EditUserDetailsDialog(user: User?, colors: ProfileColors, onDismiss: () -> Unit, onSave: (User) -> Unit) {
    var name by remember { mutableStateOf(user?.fullName ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user) { if(user != null) { name = user.fullName; email = user.email } }

    Dialog(onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Edit Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)

                val tfColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.border,
                    focusedLabelColor = colors.primary,
                    unfocusedLabelColor = colors.textSecondary,
                    errorBorderColor = Color.Red,
                    errorLabelColor = Color.Red
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = tfColors,
                    isError = errorMessage != null && name.isBlank()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = tfColors,
                    isError = errorMessage != null
                )

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = Color.Red, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = colors.textSecondary) }
                    Button(
                        onClick = {
                            if (user != null) {
                                when {
                                    name.isBlank() || email.isBlank() -> errorMessage = "Please fill all fields"
                                    !email.contains("@") -> errorMessage = "Invalid email format"
                                    else -> onSave(user.copy(fullName = name, email = email))
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun EditDailyTargetsDialog(s: Int, w: Int, c: Int, colors: ProfileColors, onDismiss: () -> Unit, onSave: (Int, Int, Int) -> Unit) {
    var sTxt by remember { mutableStateOf(s.toString()) }
    var wTxt by remember { mutableStateOf(w.toString()) }
    var cTxt by remember { mutableStateOf(c.toString()) }

    Dialog(onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Edit Goals", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                val tfColors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.textPrimary, unfocusedTextColor = colors.textPrimary, focusedBorderColor = colors.primary, unfocusedBorderColor = colors.border, focusedLabelColor = colors.primary, unfocusedLabelColor = colors.textSecondary)
                OutlinedTextField(value = sTxt, onValueChange = { if(it.all{c->c.isDigit()}) sTxt=it }, label = { Text("Steps") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), colors = tfColors)
                OutlinedTextField(value = wTxt, onValueChange = { if(it.all{c->c.isDigit()}) wTxt=it }, label = { Text("Workout (min)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), colors = tfColors)
                OutlinedTextField(value = cTxt, onValueChange = { if(it.all{c->c.isDigit()}) cTxt=it }, label = { Text("Calories") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), colors = tfColors)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = colors.textSecondary) }
                    Button(onClick = { onSave(sTxt.toIntOrNull()?:s, wTxt.toIntOrNull()?:w, cTxt.toIntOrNull()?:c) }, colors = ButtonDefaults.buttonColors(containerColor = colors.primary)) { Text("Save", color = Color.White) }
                }
            }
        }
    }
}