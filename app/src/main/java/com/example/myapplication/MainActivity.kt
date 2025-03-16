package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.views.LoginScreen
import com.example.myapplication.navigation.AppNavigation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.firebase.messaging.FirebaseMessaging
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import com.example.myapplication.data.UserPreferencesManager
import android.content.SharedPreferences

class MainActivity : ComponentActivity() {
    private lateinit var preferencesManager: UserPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        preferencesManager = UserPreferencesManager(this)
        
        // Solicitar permiso de notificaciones
        askNotificationPermission()
        
        setContent {
            var fcmToken by remember { mutableStateOf("Cargando token...") }
            var isDarkTheme by remember { mutableStateOf(preferencesManager.isDarkThemeEnabled) }
            
            // Obtener el token de FCM
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    fcmToken = task.result
                    Log.d("FCM", "Token: $fcmToken")
                } else {
                    fcmToken = "Error al obtener el token"
                    Log.e("FCM", "Error al obtener el token", task.exception)
                }
            }

            // Observar cambios en el tema
            DisposableEffect(Unit) {
                val observer = object : SharedPreferences.OnSharedPreferenceChangeListener {
                    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
                        if (key == "dark_theme") {
                            isDarkTheme = preferencesManager.isDarkThemeEnabled
                        }
                    }
                }
                preferencesManager.registerOnSharedPreferenceChangeListener(observer)
                onDispose {
                    preferencesManager.unregisterOnSharedPreferenceChangeListener(observer)
                }
            }

            MyApplicationTheme(
                darkTheme = isDarkTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        var isLoggedIn by remember { mutableStateOf(false) }
                        
                        if (isLoggedIn) {
                            val navController = rememberNavController()
                            AppNavigation(navController)
                        } else {
                            LoginScreen(onLoginSuccess = { isLoggedIn = true })
                        }
                    }
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}