package com.example.myapplication.views

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.viewmodel.UserPreferencesViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.example.myapplication.viewmodel.ThemeViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

data class Language(
    val code: String,
    val name: String
)

val availableLanguages = listOf(
    Language("es", "Español"),
    Language("en", "English"),
    Language("fr", "Français"),
    Language("de", "Deutsch"),
    Language("it", "Italiano"),
    Language("pt", "Português"),
    Language("ca", "Català")
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun UserPreferencesScreen(
    viewModel: UserPreferencesViewModel = viewModel(),
    themeViewModel: ThemeViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isLanguageMenuExpanded by remember { mutableStateOf(false) }
    var showLocationPermissionDialog by remember { mutableStateOf(false) }
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsStateWithLifecycle()

    // Estado de los permisos de ubicación
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    ) { permissionsResult ->
        if (permissionsResult.all { it.value }) {
            // Si todos los permisos fueron concedidos, actualizar ubicación
            viewModel.requestLocationUpdate(context)
        }
    }

    // Actualizar último acceso cuando se abre la pantalla
    LaunchedEffect(Unit) {
        viewModel.updateLastAccess()
        viewModel.startUsageTracking()
    }

    // Detener el tracking cuando se cierra la pantalla
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopUsageTracking()
        }
    }

    if (showLocationPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showLocationPermissionDialog = false },
            title = { Text("Permiso de ubicación") },
            text = { Text("Necesitamos acceso a tu ubicación para mostrar tu última ubicación. ¿Nos permites acceder a ella?") },
            confirmButton = {
                TextButton(onClick = {
                    showLocationPermissionDialog = false
                    locationPermissions.launchMultiplePermissionRequest()
                }) {
                    Text("Permitir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationPermissionDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preferencias de Usuario") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nombre de usuario
            OutlinedTextField(
                value = uiState.username,
                onValueChange = { viewModel.updateUsername(it) },
                label = { Text("Nombre de usuario") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            // Tema oscuro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = if (isDarkTheme) "Modo oscuro activado" else "Modo claro activado",
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (isDarkTheme) "Modo oscuro" else "Modo claro",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { themeViewModel.updateTheme(it) },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Idioma preferido
            ExposedDropdownMenuBox(
                expanded = isLanguageMenuExpanded,
                onExpandedChange = { isLanguageMenuExpanded = it },
            ) {
                OutlinedTextField(
                    value = availableLanguages.find { it.code == uiState.preferredLanguage }?.name ?: "Español",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Idioma") },
                    leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = isLanguageMenuExpanded,
                    onDismissRequest = { isLanguageMenuExpanded = false }
                ) {
                    availableLanguages.forEach { language ->
                        DropdownMenuItem(
                            text = { Text(language.name) },
                            onClick = {
                                viewModel.updatePreferredLanguage(language.code)
                                isLanguageMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Volumen de notificaciones
            Column {
                Text(
                    "Volumen de notificaciones: ${(uiState.notificationVolume * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = uiState.notificationVolume,
                    onValueChange = { viewModel.updateNotificationVolume(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Información adicional
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("Último acceso") },
                        supportingContent = { Text(uiState.lastAccess) },
                        leadingContent = { Icon(Icons.Default.AccessTime, contentDescription = null) }
                    )
                    
                    ListItem(
                        headlineContent = { Text("Última ubicación") },
                        supportingContent = { 
                            Column {
                                if (uiState.isUpdatingLocation) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else if (uiState.lastLocation.isEmpty()) {
                                    Button(
                                        onClick = {
                                            if (locationPermissions.allPermissionsGranted) {
                                                viewModel.requestLocationUpdate(context)
                                            } else {
                                                showLocationPermissionDialog = true
                                            }
                                        }
                                    ) {
                                        Text("Actualizar ubicación")
                                    }
                                } else {
                                    Text(uiState.lastLocation)
                                    TextButton(
                                        onClick = {
                                            if (locationPermissions.allPermissionsGranted) {
                                                viewModel.requestLocationUpdate(context)
                                            } else {
                                                showLocationPermissionDialog = true
                                            }
                                        }
                                    ) {
                                        Text("Actualizar")
                                    }
                                }
                            }
                        },
                        leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                    )
                    
                    ListItem(
                        headlineContent = { Text("Tiempo total de uso") },
                        supportingContent = { Text(uiState.totalUsageTime) },
                        leadingContent = { Icon(Icons.Default.Timer, contentDescription = null) }
                    )
                }
            }
        }
    }
} 