package com.example.myapplication.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import android.net.Uri
import com.example.myapplication.views.TasksScreen
import com.example.myapplication.views.CameraView
import com.example.myapplication.views.StudentScreen
import com.example.myapplication.views.UserPreferencesScreen

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Tasks : Screen("tasks", "Tareas", Icons.Default.List)
    object Camera : Screen("camera", "Cámara", Icons.Default.Camera)
    object Students : Screen("students", "Estudiantes", Icons.Default.Person)
    object UserPreferences : Screen("preferences", "Preferencias", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(navController: NavHostController) {
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                val items = listOf(Screen.Tasks, Screen.Camera, Screen.Students, Screen.UserPreferences)
                
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Tasks.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Tasks.route) {
                TasksScreen()
            }
            
            composable(Screen.Camera.route) {
                CameraView(photoUri = photoUri, onPhotoTaken = { uri -> photoUri = uri })
            }
            
            composable(Screen.Students.route) {
                StudentScreen()
            }

            composable(Screen.UserPreferences.route) {
                UserPreferencesScreen(
                    onNavigateBack = { navController.navigateUp() }
                )
            }
        }
    }
} 