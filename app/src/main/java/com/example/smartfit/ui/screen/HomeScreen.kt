package com.example.smartfit.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.smartfit.data.room.CalorieIntakeEntity
import com.example.smartfit.data.room.StepEntity
import com.example.smartfit.data.room.WorkoutEntity
import com.example.smartfit.repository.ActivityRepository
import com.example.smartfit.viewModel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddActivityClick: () -> Unit,
    onActivityClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HomeViewModel
) {
    // --- Data Collection ---
    val allSteps by viewModel.allSteps.collectAsState()
    val allWorkouts by viewModel.allWorkouts.collectAsState()
    val allFood by viewModel.allFood.collectAsState()
    val quote by viewModel.dailyQuote.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val stepGoal by viewModel.stepGoal.collectAsState()
    val workoutGoal by viewModel.workoutGoal.collectAsState()
    val calorieGoal by viewModel.calorieGoal.collectAsState()

    val currentSteps by viewModel.stepCount.collectAsState()

    // --- Date & Totals Logic ---
    val todayString = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val displayDate = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date()) }

    val totalWorkout = allWorkouts.filter { it.date == todayString }.sumOf { it.durationMinutes }
    val totalCalories = allFood.filter { it.date == todayString }.sumOf { it.calories.toInt() }

    val weeklyData = remember(allSteps, allWorkouts, allFood) {
        calculateWeeklyData(allSteps, allWorkouts, allFood)
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),

            verticalArrangement = Arrangement.spacedBy(32.dp),

            contentPadding = PaddingValues(top = 24.dp, bottom = 40.dp)
        ) {
            // 1. Header Section (Containerized)
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                        Text(
                            text = "Welcome to SmartFit 👋",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = displayDate,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your all-in-one fitness companion is here!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. Summaries Today
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeader("Today's Summary")
                    ModernGradientCard(
                        title = "Steps",
                        value = currentSteps.toString(),
                        goal = stepGoal,
                        suffix = "steps",
                        icon = Icons.AutoMirrored.Filled.DirectionsRun,
                        gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB))
                    )

                    ModernGradientCard(
                        title = "Workout",
                        value = totalWorkout.toString(),
                        goal = workoutGoal,
                        suffix = "minutes",
                        icon = Icons.Filled.FitnessCenter,
                        gradientColors = listOf(Color(0xFFA855F7), Color(0xFF9333EA))
                    )

                    ModernGradientCard(
                        title = "Calories",
                        value = totalCalories.toString(),
                        goal = calorieGoal,
                        suffix = "kcal",
                        icon = Icons.Default.LocalFireDepartment,
                        gradientColors = listOf(Color(0xFFF97316), Color(0xFFEA580C))
                    )
                }
            }

            // 3. Weekly Summary
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeader("Weekly Summary")
                    // Chart 1: Steps (Line)
                    ModernChartCard("Weekly Steps") {
                        WeeklyLineChart(weeklyData)
                    }

                    // Chart 2: Overview (Bar)
                    ModernChartCard("Weekly Overview") {
                        WeeklyBarChart(weeklyData)
                    }
                }
            }

            // 4. Daily Inspiration
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeader("Daily Workout Suggestion")
                    ModernTipCard(quote)
                }
            }

            // 5. Featured Workout
            item {
                FeaturedWorkoutCardModern()
            }
        }
    }
}

// --- COMPONENTS ---

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
fun ModernGradientCard(
    title: String,
    value: String,
    goal: Int,
    suffix: String,
    icon: ImageVector,
    gradientColors: List<Color>
) {
    val safeValue = value.replace(",", "").toIntOrNull() ?: 0
    val progressTarget = if (goal > 0) (safeValue.toFloat() / goal).coerceIn(0f, 1f) else 0f
    val progress by animateFloatAsState(targetValue = progressTarget, label = "Progress")

    Card(
        modifier = Modifier.fillMaxWidth().height(170.dp), // Increased height vertically
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradientColors))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween // Distribute content vertically
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(10.dp)
                    ) {
                        Icon(icon, contentDescription = null, tint = Color.White)
                    }
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "of $goal $suffix",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.2f),
                )
            }
        }
    }
}

@Composable
fun ModernChartCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            Box(Modifier.height(220.dp).fillMaxWidth()) { content() }
        }
    }
}

@Composable
fun ModernTipCard(tip: ActivityRepository.WorkoutTip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) { // Increased padding
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.TipsAndUpdates,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = tip.workoutName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Target: ${tip.target}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tip.summary,
                style = MaterialTheme.typography.bodyLarge, // Slightly larger text
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun FeaturedWorkoutCardModern() {
    val baseUrl = "https://loremflickr.com/800/400/fitness,gym"
    val randomImageUrl = remember { "$baseUrl?lock=${System.currentTimeMillis()}" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.height(220.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(randomImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Featured Workout",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 100f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        " FEATURED ",
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Full Body Blast",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Start your day with energy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// 1. Helper Function for "Clean" Y-Axis Numbers
fun getSmartYAxisLabels(maxVal: Float): Pair<Float, List<String>> {
    if (maxVal <= 0f) return 100f to listOf("100", "66", "33", "0")

    val gridCount = 3
    val rawStep = maxVal / gridCount
    val mag = Math.pow(10.0, kotlin.math.floor(kotlin.math.log10(rawStep.toDouble()))).toFloat()
    val normalizedStep = rawStep / mag
    val cleanStepFactor = kotlin.math.ceil(normalizedStep).toInt()
    val cleanStep = cleanStepFactor * mag
    val niceMax = cleanStep * gridCount

    val labels = listOf(
        niceMax.toInt().toString(),
        (cleanStep * 2).toInt().toString(),
        cleanStep.toInt().toString(),
        "0"
    )
    return niceMax to labels
}

// 2. Weekly Line Chart (Steps)
@Composable
fun WeeklyLineChart(data: List<WeeklyData>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    // Visible grid color for Light Mode
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    if (data.isEmpty()) return

    val rawMax = remember(data) { data.maxOfOrNull { it.steps }?.toFloat() ?: 1f }
    val (niceMax, yAxisLabels) = remember(rawMax) { getSmartYAxisLabels(rawMax) }

    var selectedDataPoint by remember { mutableStateOf<WeeklyData?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Y-Axis Labels
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    yAxisLabels.forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurface.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(35.dp)
                        )
                    }
                }

                // Chart Canvas
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val spacePerPoint = size.width / max(data.size - 1, 1)
                                val index = (offset.x / spacePerPoint).roundToInt().coerceIn(0, data.size - 1)
                                selectedDataPoint = data[index]
                            }
                        }
                ) {
                    val spacePerPoint = size.width / max(data.size - 1, 1)
                    val path = Path()

                    // Grid Lines
                    drawLine(gridColor, Offset(0f, 0f), Offset(size.width, 0f))
                    drawLine(gridColor, Offset(0f, size.height * 0.33f), Offset(size.width, size.height * 0.33f))
                    drawLine(gridColor, Offset(0f, size.height * 0.66f), Offset(size.width, size.height * 0.66f))
                    drawLine(gridColor, Offset(0f, size.height), Offset(size.width, size.height))

                    data.forEachIndexed { index, day ->
                        val x = index * spacePerPoint
                        val y = size.height - (day.steps / niceMax * size.height)

                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)

                        drawCircle(Color.White, radius = 6.dp.toPx(), center = Offset(x, y))
                        drawCircle(primaryColor, radius = 4.dp.toPx(), center = Offset(x, y))
                    }
                    drawPath(path, primaryColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                }
            }

            // Purple Light Tooltip
            if (selectedDataPoint != null) {
                Popup(
                    alignment = Alignment.Center,
                    onDismissRequest = { selectedDataPoint = null }
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = selectedDataPoint!!.date,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${selectedDataPoint!!.steps} Steps",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // X-Axis Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 47.dp)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { day ->
                Text(
                    text = day.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurface.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

// 3. Weekly Bar Chart (Overview)
@Composable
fun WeeklyBarChart(data: List<WeeklyData>) {
    val workoutColor = Color(0xFFA855F7)
    val caloriesColor = Color(0xFFF97316)
    val onSurface = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    if (data.isEmpty()) return

    // Calculate Scales
    val maxWorkout = remember(data) { data.maxOfOrNull { it.workout }?.toFloat()?.coerceAtLeast(1f) ?: 1f }
    val maxCalories = remember(data) { data.maxOfOrNull { it.calories }?.toFloat()?.coerceAtLeast(1f) ?: 1f }
    val (niceMax, yAxisLabels) = remember(maxCalories) { getSmartYAxisLabels(maxCalories) }

    var selectedDataPoint by remember { mutableStateOf<WeeklyData?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = workoutColor, label = "Workout")
            Spacer(modifier = Modifier.width(16.dp))
            LegendItem(color = caloriesColor, label = "Calories")
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Y-Axis Labels
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    yAxisLabels.forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurface.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(35.dp)
                        )
                    }
                }

                // Chart Canvas
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val space = size.width / data.size
                                val index = (offset.x / space).toInt().coerceIn(0, data.size - 1)
                                selectedDataPoint = data[index]
                            }
                        }
                ) {
                    val space = size.width / data.size
                    val barGroupWidth = space * 0.6f
                    val barWidth = barGroupWidth / 2.2f

                    // Grid Lines
                    drawLine(gridColor, Offset(0f, 0f), Offset(size.width, 0f))
                    drawLine(gridColor, Offset(0f, size.height * 0.33f), Offset(size.width, size.height * 0.33f))
                    drawLine(gridColor, Offset(0f, size.height * 0.66f), Offset(size.width, size.height * 0.66f))
                    drawLine(gridColor, Offset(0f, size.height), Offset(size.width, size.height))

                    data.forEachIndexed { index, day ->
                        val slotCenter = (index * space) + (space / 2)
                        val workoutX = slotCenter - barWidth - 2.dp.toPx()
                        val caloriesX = slotCenter + 2.dp.toPx()

                        val workoutHeight = (day.workout / maxWorkout) * size.height
                        val caloriesHeight = (day.calories / niceMax) * size.height

                        // Workout Bar
                        drawRoundRect(
                            color = workoutColor,
                            topLeft = Offset(workoutX, size.height - workoutHeight),
                            size = Size(barWidth, workoutHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                        )

                        // Calories Bar
                        drawRoundRect(
                            color = caloriesColor,
                            topLeft = Offset(caloriesX, size.height - caloriesHeight),
                            size = Size(barWidth, caloriesHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                        )
                    }
                }
            }

            // Popup Tooltip
            if (selectedDataPoint != null) {
                Popup(
                    alignment = Alignment.Center,
                    onDismissRequest = { selectedDataPoint = null }
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = selectedDataPoint!!.date,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Workout: ${selectedDataPoint!!.workout} min",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFA855F7)
                            )
                            Text(
                                text = "Calories: ${selectedDataPoint!!.calories} kcal",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFF97316)
                            )
                        }
                    }
                }
            }
        }

        // X-Axis Labels (MATCHING LINE CHART STYLE EXACTLY)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 47.dp) // Aligns with Chart area (35dp label + 12dp padding)
                .padding(top = 8.dp),
            // Changed from weight distribution to SpaceBetween to match Line Chart
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { day ->
                Text(
                    text = day.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurface.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

// 4. Legend Item Component
@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// --- DATA MODELS ---

data class WeeklyData(val date: String, val steps: Int, val workout: Int, val calories: Int)

fun calculateWeeklyData(
    steps: List<StepEntity>,
    workouts: List<WorkoutEntity>,
    food: List<CalorieIntakeEntity>
): List<WeeklyData> {
    val calendar = Calendar.getInstance()
    val data = mutableListOf<WeeklyData>()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayLabelFormat = SimpleDateFormat("EEE", Locale.getDefault()) // Formats date to "Mon", "Tue"

    for (i in 6 downTo 0) {
        calendar.time = Date()
        calendar.add(Calendar.DAY_OF_YEAR, -i)
        val dateString = dateFormat.format(calendar.time)
        val dayLabel = dayLabelFormat.format(calendar.time) // This is the dynamic X-axis label

        val daySteps = steps.filter { it.date == dateString }.sumOf { it.steps }
        val dayWorkout = workouts.filter { it.date == dateString }.sumOf { it.durationMinutes }
        val dayCalories = food.filter { it.date == dateString }.sumOf { it.calories.toInt() }

        data.add(WeeklyData(dayLabel, daySteps, dayWorkout, dayCalories))
    }
    return data
}