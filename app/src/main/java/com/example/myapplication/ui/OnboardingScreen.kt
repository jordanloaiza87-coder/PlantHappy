package com.example.myapplication.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* Opcional: mostrar un Toast o saltar de página al aceptar */ }
    )
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* Opcional: manejar rechazos/aceptaciones */ }
    )

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicadores de página
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (pagerState.currentPage == index) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                // Botón Siguiente / Comenzar
                Button(
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinished()
                        }
                    }
                ) {
                    Text(if (pagerState.currentPage == 2) "Comenzar" else "Siguiente")
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            when (page) {
                0 -> OnboardingPage(
                    icon = Icons.Default.Eco,
                    title = "Bienvenido a PlantHappy",
                    description = "Tu asistente personal para el cuidado de plantas. Descubre qué necesitan y mantén tu jardín vibrante y lleno de vida."
                )
                1 -> OnboardingPage(
                    icon = Icons.Default.NotificationsActive,
                    title = "Alertas a tiempo",
                    description = "Para avisarte justo cuando tu planta tiene sed o hace mucho frío, necesitamos permiso para enviarte notificaciones. ¡Nunca volverás a olvidar regarlas!",
                    permissionButtonText = "Habilitar Notificaciones",
                    onPermissionClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
                2 -> OnboardingPage(
                    icon = Icons.Default.LocationOn,
                    title = "Clima Inteligente",
                    description = "Usamos tu ubicación para conocer el clima, la humedad y la luz natural de tu zona de manera automática. Así te damos recomendaciones perfectas.",
                    permissionButtonText = "Habilitar Ubicación",
                    onPermissionClick = {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }
                )
            }
        }
    }
}

@Composable
fun OnboardingPage(
    icon: ImageVector,
    title: String,
    description: String,
    permissionButtonText: String? = null,
    onPermissionClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = description,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (permissionButtonText != null && onPermissionClick != null) {
            FilledTonalButton(onClick = onPermissionClick) {
                Text(permissionButtonText)
            }
        }
    }
}