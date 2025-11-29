package com.example.smartfit.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.smartfit.SmartFitApplication
import com.example.smartfit.data.room.CalorieIntakeEntity
import com.example.smartfit.data.room.StepEntity
import com.example.smartfit.data.room.WorkoutEntity
import com.example.smartfit.viewModel.ActivityViewModel
import com.example.smartfit.viewModel.FoodViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    activityViewModel: ActivityViewModel
) {
    val context = LocalContext.current
    val foodViewModel: FoodViewModel = viewModel(
        factory = FoodViewModel.Factory(context.applicationContext as SmartFitApplication)
    )

    // --- Date State ---
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateStr by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val isToday = selectedDateStr == todayStr

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance().apply { timeInMillis = millis }
                        selectedDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- Data Collection ---
    val allSteps by activityViewModel.allSteps.collectAsState()
    val allWorkouts by activityViewModel.allWorkouts.collectAsState()
    val allFood by activityViewModel.allFood.collectAsState()

    val filteredSteps = allSteps.filter { it.date == selectedDateStr }
    val filteredWorkouts = allWorkouts.filter { it.date == selectedDateStr }
    val filteredFood = allFood.filter { it.date == selectedDateStr }

    // --- UI State ---
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Steps", "Workouts", "Food")

    // Dialog States
    var showAddDialog by remember { mutableStateOf(false) }
    var showStepDialog by remember { mutableStateOf(false) }

    // Edit States
    var stepToEdit by remember { mutableStateOf<StepEntity?>(null) }
    var workoutToEdit by remember { mutableStateOf<WorkoutEntity?>(null) }
    var foodToEdit by remember { mutableStateOf<CalorieIntakeEntity?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Activity Log", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("Tracking your daily progress", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )

                Surface(
                    onClick = { showDatePicker = true },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isToday) "Today, $selectedDateStr" else selectedDateStr,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                }
            }
        },
        floatingActionButton = {
            val showFab = if (selectedTabIndex == 0) {
                isToday && filteredSteps.isEmpty()
            } else {
                isToday
            }

            if (showFab) {
                ExtendedFloatingActionButton(
                    text = {
                        Text(
                            when(selectedTabIndex) {
                                0 -> "Add Steps"
                                1 -> "Log Workout"
                                else -> "Log Food"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    icon = {
                        Icon(
                            when(selectedTabIndex) {
                                0 -> Icons.AutoMirrored.Filled.DirectionsWalk
                                1 -> Icons.Default.FitnessCenter
                                else -> Icons.Default.Restaurant
                            }, null
                        )
                    },
                    onClick = {
                        when (selectedTabIndex) {
                            0 -> { stepToEdit = null; showStepDialog = true }
                            1 -> { workoutToEdit = null; showAddDialog = true }
                            else -> { foodToEdit = null; showAddDialog = true }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                divider = { Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)) },
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTabIndex == i,
                        onClick = { selectedTabIndex = i },
                        text = { Text(title, fontWeight = if(selectedTabIndex == i) FontWeight.Bold else FontWeight.Medium) },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> { // Steps
                        if (filteredSteps.isEmpty()) item { EmptyStateModern("No steps logged today.") }
                        items(filteredSteps) { step ->
                            StepRowCard(
                                step = step,
                                isEditable = isToday,
                                onDelete = { activityViewModel.deleteStep(step) },
                                onEdit = { stepToEdit = step; showStepDialog = true }
                            )
                        }
                    }
                    1 -> { // Workouts
                        if (filteredWorkouts.isEmpty()) item { EmptyStateModern("No workouts logged.") }
                        items(filteredWorkouts) { workout ->
                            WorkoutRowCard(
                                workout = workout,
                                isEditable = isToday,
                                onDelete = { activityViewModel.deleteWorkout(workout) },
                                onEdit = { workoutToEdit = workout; showAddDialog = true }
                            )
                        }
                    }
                    2 -> { // Food
                        if (filteredFood.isEmpty()) item { EmptyStateModern("No meals logged.") }
                        items(filteredFood) { food ->
                            FoodRowCard(
                                food = food,
                                isEditable = isToday,
                                onDelete = { activityViewModel.deleteFood(food) },
                                onEdit = { foodToEdit = food; showAddDialog = true }
                            )
                        }
                    }
                }
            }
        }

        if (showStepDialog) {
            ModernStepEntryDialog(
                initialSteps = stepToEdit?.steps ?: 0,
                initialDesc = stepToEdit?.description ?: "",
                isEditMode = stepToEdit != null,
                onDismiss = { showStepDialog = false; stepToEdit = null },
                onConfirm = { steps, desc ->
                    activityViewModel.saveSteps(steps, desc)
                    showStepDialog = false
                    stepToEdit = null
                }
            )
        }

        if (showAddDialog) {
            ModernAddActivityDialog(
                mode = if (selectedTabIndex == 1) "Workout" else "Food",
                onDismiss = { showAddDialog = false; workoutToEdit = null; foodToEdit = null },
                foodViewModel = foodViewModel,
                initialWorkout = workoutToEdit,
                initialFood = foodToEdit,
                onSaveWorkout = { type, duration, desc ->
                    if (workoutToEdit != null) {
                        activityViewModel.updateWorkout(workoutToEdit!!.copy(workoutType = type, durationMinutes = duration, description = desc))
                    } else {
                        activityViewModel.addWorkout(type, duration, desc)
                    }
                    showAddDialog = false
                    workoutToEdit = null
                },
                onSaveFood = { name, qty, unit, baseCals, desc, image ->
                    val dateObj = try { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDateStr) } catch (_: Exception) { Date() } ?: Date()
                    if (foodToEdit != null) {
                        activityViewModel.deleteFood(foodToEdit!!)
                        activityViewModel.addFood(dateObj, name, qty, unit, baseCals, desc, image)
                    } else {
                        activityViewModel.addFood(dateObj, name, qty, unit, baseCals, desc, image)
                    }
                    showAddDialog = false
                    foodToEdit = null
                }
            )
        }
    }
}

// --- Cards ---

@Composable
fun getCardColor(): Color {
    val isDark = isSystemInDarkTheme()
    return if (isDark) Color(0xFF424242) else MaterialTheme.colorScheme.surface
}

@Composable
fun StepRowCard(step: StepEntity, isEditable: Boolean, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = getCardColor()),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("${step.steps} Steps", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    if (step.description.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text("Description: ${step.description}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (isEditable) {
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", tint = Color.Red) }
                }
            }
        }
    }
}

@Composable
fun WorkoutRowCard(workout: WorkoutEntity, isEditable: Boolean, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = getCardColor()),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 20.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(Modifier.size(52.dp).clip(CircleShape).background(Color(0xFFA855F7).copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.FitnessCenter, null, tint = Color(0xFFA855F7), modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(workout.workoutType, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    if (workout.description.isNotEmpty()) {
                        Text("Description: ${workout.description}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${workout.durationMinutes} min", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                if (isEditable) {
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, "Delete", tint = Color.Red, modifier = Modifier.size(18.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun FoodRowCard(food: CalorieIntakeEntity, isEditable: Boolean, onDelete: () -> Unit, onEdit: () -> Unit) {
    val formattedQty = if (food.quantity % 1.0 == 0.0) food.quantity.toInt().toString() else food.quantity.toString()
    val imageUrl = try { food.image } catch (e: Exception) { null }
    val fullImageUrl = if (!imageUrl.isNullOrEmpty() && !imageUrl.startsWith("http")) "https://img.spoonacular.com/ingredients_100x100/$imageUrl" else imageUrl

    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = getCardColor()),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 20.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(Modifier.size(52.dp).clip(CircleShape).background(Color(0xFFF97316).copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    if (fullImageUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(fullImageUrl),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Restaurant, null, tint = Color(0xFFF97316), modifier = Modifier.size(26.dp))
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(food.foodName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    // Added Quantity Display
                    Text("$formattedQty ${food.unit}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    if (food.description.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text("Description: ${food.description}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("${food.calories.toInt()} kcal", fontWeight = FontWeight.Bold, color = Color(0xFFF97316), style = MaterialTheme.typography.titleMedium)
                if (isEditable) {
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, "Delete", tint = Color.Red, modifier = Modifier.size(18.dp)) }
                    }
                }
            }
        }
    }
}

// --- Dialogs ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernAddActivityDialog(
    mode: String,
    onDismiss: () -> Unit,
    foodViewModel: FoodViewModel,
    initialWorkout: WorkoutEntity? = null,
    initialFood: CalorieIntakeEntity? = null,
    onSaveWorkout: (String, Int, String) -> Unit,
    onSaveFood: (String, Double, String, Double, String, String?) -> Unit
) {
    val isFood = mode == "Food"
    val isEdit = initialWorkout != null || initialFood != null

    // Workout State
    val workoutTypes = listOf("Running", "Cycling", "Swimming", "Yoga", "Gym", "HIIT", "Other")
    var selectedWorkout by remember { mutableStateOf(initialWorkout?.workoutType ?: workoutTypes.first()) }
    var customWorkoutName by remember { mutableStateOf(if (initialWorkout != null && initialWorkout.workoutType !in workoutTypes) initialWorkout.workoutType else "") }
    var durationText by remember { mutableStateOf(initialWorkout?.durationMinutes?.toString() ?: "") }

    // Food State
    var searchQuery by remember { mutableStateOf(initialFood?.foodName ?: "") }
    var qtyText by remember { mutableStateOf(initialFood?.quantity?.toString() ?: "1") }

    // Common State
    var description by remember { mutableStateOf(initialWorkout?.description ?: initialFood?.description ?: "") }

    var expanded by remember { mutableStateOf(false) }
    val searchResults by foodViewModel.searchResults.collectAsState()
    val foodDetail by foodViewModel.selectedFoodDetail.collectAsState()
    val selectedFoodImage by foodViewModel.selectedFoodImage.collectAsState()
    val isLoading by foodViewModel.isLoading.collectAsState()

    Dialog(onDismissRequest = { foodViewModel.clearResults(); onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    if (isEdit) "Edit $mode" else "Log $mode",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                if (!isFood) {
                    // --- WORKOUT FORM ---
                    Box {
                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Text(if (selectedWorkout == "Other" && customWorkoutName.isNotEmpty()) customWorkoutName else selectedWorkout, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            workoutTypes.forEach { t ->
                                DropdownMenuItem(text = { Text(t) }, onClick = {
                                    selectedWorkout = t
                                    if(t != "Other") customWorkoutName = ""
                                    expanded = false
                                })
                            }
                        }
                    }
                    if (selectedWorkout == "Other") {
                        OutlinedTextField(
                            value = customWorkoutName,
                            onValueChange = { customWorkoutName = it },
                            label = { Text("Custom Activity Name") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { if(it.all { c->c.isDigit() }) durationText=it },
                        label = { Text("Duration (minutes)") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // --- FOOD FORM ---
                    if (foodDetail == null && !isEdit) {
                        // Search Mode
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = searchQuery, onValueChange = { searchQuery = it }, label = { Text("Search Food") },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true
                            )
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = { foodViewModel.searchFood(searchQuery) }, shape = RoundedCornerShape(12.dp), modifier = Modifier.height(56.dp)) { Text("Go") }
                        }
                        if(isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())

                        LazyColumn(modifier = Modifier.height(200.dp)) {
                            items(searchResults) { item ->
                                val imageUrl = "https://img.spoonacular.com/ingredients_100x100/${item.image}"
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { foodViewModel.onFoodSelected(item) }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if(!item.image.isNullOrEmpty()) {
                                        Image(rememberAsyncImagePainter(imageUrl), null, Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                        Spacer(Modifier.width(12.dp))
                                    }
                                    Text(item.name, fontWeight = FontWeight.Medium)
                                }
                                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            }
                        }
                    } else {
                        // --- DETAILS / EDIT MODE (Hardcoded Unit Display Here) ---
                        Column(
                            Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.2f), RoundedCornerShape(12.dp)).padding(16.dp)
                        ) {
                            val name = if(isEdit) initialFood!!.foodName else foodDetail?.name ?: ""
                            val unit = if(isEdit) initialFood!!.unit else foodDetail?.unit ?: "unit" // Used for calculation
                            val baseCals = if(isEdit) (initialFood!!.calories / initialFood.quantity) else foodViewModel.getCaloriesPerUnit()

                            Text("Selected: $name", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                            Spacer(Modifier.height(8.dp))

                            // 1. HARDCODED "per unit"
                            Text(
                                text = "Base Calories: ${baseCals.toInt()} kcal per unit",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(16.dp))

                            // 2. HARDCODED "Quantity (unit)"
                            OutlinedTextField(
                                value = qtyText,
                                onValueChange = { if(it.all { c->c.isDigit() || c=='.' }) qtyText = it },
                                label = { Text("Quantity (unit)") },
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )

                            val total = (qtyText.toDoubleOrNull() ?: 0.0) * baseCals
                            Spacer(Modifier.height(8.dp))
                            Text("Total: ${total.toInt()} kcal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.End))
                        }
                        if(!isEdit) TextButton(onClick = { foodViewModel.clearResults() }) { Text("Change Selection") }
                    }
                }

                // Description Field (Last Input)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Notes") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { foodViewModel.clearResults(); onDismiss() }) { Text("Close") }
                    Button(
                        onClick = {
                            if (isFood) {
                                val name = if(isEdit) initialFood!!.foodName else foodDetail!!.name
                                // We still save the API's actual unit (e.g. "g") so calculations remain correct
                                val unit = if(isEdit) initialFood!!.unit else foodDetail!!.unit ?: "unit"
                                val base = if(isEdit) (initialFood!!.calories / initialFood.quantity) else foodViewModel.getCaloriesPerUnit()
                                val qty = qtyText.toDoubleOrNull() ?: 1.0
                                val img = if(isEdit) initialFood!!.image else selectedFoodImage

                                onSaveFood(name, qty, unit, base, description, img)
                            } else {
                                val finalType = if (selectedWorkout == "Other") customWorkoutName else selectedWorkout
                                onSaveWorkout(finalType, durationText.toIntOrNull()?:0, description)
                            }
                        },
                        enabled = !isFood || (foodDetail != null || isEdit),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Save") }
                }
            }
        }
    }
}

// Re-used Step Entry Dialog
@Composable
fun ModernStepEntryDialog(initialSteps: Int, initialDesc: String, isEditMode: Boolean, onDismiss: () -> Unit, onConfirm: (Int, String) -> Unit) {
    var s by remember { mutableStateOf(if(initialSteps>0) initialSteps.toString() else "") }
    var d by remember { mutableStateOf(initialDesc) }
    Dialog(onDismiss) {
        Card(shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(24.dp)) {
                Text(if(isEditMode) "Update Steps" else "Log Steps", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = s, onValueChange = { if(it.all{c->c.isDigit()}) s=it }, label = { Text("Total Steps") }, shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = d, onValueChange = { d=it }, label = { Text("Note") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = { onConfirm(s.toIntOrNull()?:0, d) }, shape = RoundedCornerShape(12.dp)) { Text("Save") }
                }
            }
        }
    }
}

@Composable
fun EmptyStateModern(msg: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.History, null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(12.dp))
        Text(msg, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}