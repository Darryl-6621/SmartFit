package com.example.smartfit.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.smartfit.viewModel.HomeViewModel
import com.example.smartfit.viewModel.ActivityViewModel
import com.example.smartfit.SmartFitApplication

@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(context.applicationContext as SmartFitApplication)
    )

    val activityViewModel: ActivityViewModel = viewModel(
        factory = ActivityViewModel.Factory(context.applicationContext as SmartFitApplication)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("Home") },
                    selected = currentRoute == "home_tab",
                    onClick = { if (currentRoute != "home_tab") navController.navigate("home_tab") { popUpTo(navController.graph.startDestinationId); launchSingleTop = true; restoreState = true } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.DirectionsRun, "Activity") },
                    label = { Text("Activity") },
                    selected = currentRoute == "activity_tab",
                    onClick = { if (currentRoute != "activity_tab") navController.navigate("activity_tab") { popUpTo(navController.graph.startDestinationId); launchSingleTop = true; restoreState = true } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, "Profile") },
                    label = { Text("Profile") },
                    selected = currentRoute == "profile_tab",
                    onClick = { if (currentRoute != "profile_tab") navController.navigate("profile_tab") { popUpTo(navController.graph.startDestinationId); launchSingleTop = true; restoreState = true } }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home_tab",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home_tab") {
                HomeScreen(
                    viewModel = homeViewModel,
                    onAddActivityClick = { /* Handled in ActivityTab */ },
                    onActivityClick = { },
                    onProfileClick = { navController.navigate("profile_tab") }
                )
            }
            composable("activity_tab") {
                ActivityScreen(activityViewModel = activityViewModel)
            }
            composable("profile_tab") {
                ProfileScreen(
                    navController = navController,
                    onLogoutClick = onLogout
                )
            }
        }
    }
}