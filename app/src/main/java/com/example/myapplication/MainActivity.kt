package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.ui.AuthCoordinator
import com.example.myapplication.ui.OnboardingScreen
import com.example.myapplication.ui.PlantDashboardScreen
import com.example.myapplication.ui.HandDrawnFlowerCanvas
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.PlantViewModel
import com.example.myapplication.notification.PlantNotificationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            var showSplash by remember { mutableStateOf(true) }
            var showOnboarding by remember { mutableStateOf(!prefs.getBoolean("has_seen_onboarding", false)) }
            var isLoggedIn by remember { mutableStateOf(prefs.getBoolean("is_logged_in", false)) }
            
            // Proveemos el NotificationManager al ViewModel
            val notificationManager = remember { PlantNotificationManager(context) }
            val viewModel: PlantViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return PlantViewModel(notificationManager) as T
                    }
                }
            )
            
            val state by viewModel.uiState.collectAsState()

            MyApplicationTheme(isWaterLow = state.isWaterLow) {
                if (showSplash) {
                    FlowerLoadingScreen(
                        onLoadingFinished = { showSplash = false }
                    )
                } else if (!isLoggedIn) {
                    AuthCoordinator(
                        onAuthSuccess = {
                            prefs.edit().putBoolean("is_logged_in", true).apply()
                            isLoggedIn = true
                            
                            // Hacemos que el Onboarding SIEMPRE aparezca después de iniciar sesión
                            prefs.edit().putBoolean("has_seen_onboarding", false).apply()
                            showOnboarding = true
                        }
                    )
                } else if (showOnboarding) {
                    OnboardingScreen(
                        onFinished = {
                            prefs.edit().putBoolean("has_seen_onboarding", true).apply()
                            showOnboarding = false
                        }
                    )
                } else {
                    PlantDashboardScreen(
                        viewModel = viewModel,
                        onLogout = {
                            prefs.edit().putBoolean("is_logged_in", false).apply()
                            isLoggedIn = false
                            
                            // Reiniciamos el estado para el próximo inicio de sesión
                            prefs.edit().putBoolean("has_seen_onboarding", false).apply()
                            showOnboarding = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FlowerLoadingScreen(onLoadingFinished: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        // Animamos la línea de carga y el crecimiento de la flor (0 a 100%)
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2500, easing = LinearOutSlowInEasing)
        ) { value, _ ->
            progress = value
        }
        delay(200) // Pequeña pausa antes de mostrar el dashboard
        onLoadingFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // La misma flor del dashboard, animando su escala para que "crezca" poco a poco
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(0.3f + (0.7f * progress)) // Escala de 0.3 a 1.0 según el progreso
            ) {
                HandDrawnFlowerCanvas(
                    soilMoisture = 80f, // Para que salga feliz
                    temperature = 22f,  // Temperatura ideal
                    lightLevel = 300f   // Modo de día
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Animación de línea de carga
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .width(200.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Despertando al jardín... ${(progress * 100).toInt()}%",
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
