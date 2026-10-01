package com.example.myapplication.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.viewmodel.PlantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDashboardScreen(viewModel: PlantViewModel, onLogout: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    // Smooth animation for soil moisture changes (watering / slider drag)
    val animatedSoilMoisture by animateFloatAsState(
        targetValue = state.soilMoisture,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "soilMoistureAnim"
    )

    // Soft twilight overlay alpha (max 0.3 so text and info remain perfectly clear and readable)
    val targetAlpha = ((300f - state.lightLevel) / 300f).coerceIn(0f, 0.3f)
    val nightOverlayAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(600),
        label = "nightOverlay"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (state.lightLevel < 150f) "🌙 Buenas Noches" else "🌻 Jardín Vivo") 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Eco, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Sensores") },
                    label = { Text("Sensores") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeTab(
                    animatedSoilMoisture = animatedSoilMoisture,
                    state = state,
                    onWaterClick = { viewModel.waterPlant() }
                )
                1 -> SensorsTab(
                    animatedSoilMoisture = animatedSoilMoisture,
                    state = state,
                    viewModel = viewModel
                )
                2 -> ProfileTab(onLogout = onLogout)
            }

            // Soft Twilight Night Mode Overlay
            if (nightOverlayAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F172A).copy(alpha = nightOverlayAlpha))
                )
            }
        }
    }
}

@Composable
fun HomeTab(
    animatedSoilMoisture: Float,
    state: com.example.myapplication.model.PlantState,
    onWaterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // En Home solo mostramos la planta principal bien grande
        PlantHappyFlowerCard(
            soilMoisture = animatedSoilMoisture,
            isWaterLow = animatedSoilMoisture < 30f,
            temperature = state.temperature,
            lightLevel = state.lightLevel,
            plantName = state.plantName,
            onWaterClick = onWaterClick
        )
    }
}

@Composable
fun SensorsTab(
    animatedSoilMoisture: Float,
    state: com.example.myapplication.model.PlantState,
    viewModel: PlantViewModel
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Sensores en Tiempo Real",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SensorCard(
                title = "Humedad de Tierra",
                value = "${animatedSoilMoisture.toInt()}%",
                icon = Icons.Default.WaterDrop,
                isWarning = animatedSoilMoisture < 30f,
                modifier = Modifier.weight(1f)
            )
            SensorCard(
                title = "Temperatura",
                value = "${state.temperature.toInt()} °C",
                icon = Icons.Default.Thermostat,
                isWarning = state.temperature < 15f || state.temperature > 32f,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SensorCard(
                title = "Humedad Ambiente",
                value = "${state.airHumidity.toInt()}%",
                icon = Icons.Default.Cloud,
                isWarning = state.airHumidity < 40f,
                modifier = Modifier.weight(1f)
            )
            SensorCard(
                title = "Luz Ambiental",
                value = "${state.lightLevel.toInt()} lx",
                icon = if (state.lightLevel < 150f) Icons.Default.NightsStay else Icons.Default.WbSunny,
                isWarning = state.lightLevel < 200f,
                modifier = Modifier.weight(1f)
            )
        }

        // Recommendations / Alerts
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "💡 Diagnóstico",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                state.recommendations.forEach { recommendation ->
                    Text(
                        text = recommendation,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        // Interactive Simulator Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🎛️ Simulador",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                // Moisture slider
                Column {
                    Text("Humedad de Tierra: ${state.soilMoisture.toInt()}%")
                    Slider(
                        value = state.soilMoisture,
                        onValueChange = { viewModel.updateSoilMoisture(it) },
                        valueRange = 0f..100f
                    )
                }

                // Temperature slider
                Column {
                    Text("Temperatura: ${state.temperature.toInt()} °C")
                    Slider(
                        value = state.temperature,
                        onValueChange = { viewModel.updateTemperature(it) },
                        valueRange = 10f..40f
                    )
                }

                // Air Humidity slider
                Column {
                    Text("Humedad del Aire: ${state.airHumidity.toInt()}%")
                    Slider(
                        value = state.airHumidity,
                        onValueChange = { viewModel.updateAirHumidity(it) },
                        valueRange = 10f..100f
                    )
                }

                // Light slider
                Column {
                    Text("Luz: ${state.lightLevel.toInt()} lx (Día/Noche)")
                    Slider(
                        value = state.lightLevel,
                        onValueChange = { viewModel.updateLightLevel(it) },
                        valueRange = 0f..2000f
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileTab(onLogout: () -> Unit) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Mi Perfil",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Amante de las plantas",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileSectionTitle("Cuenta")
            ProfileListItem(
                icon = Icons.Default.Person,
                title = "Información Personal",
                subtitle = "Gestiona tus datos personales"
            )
            
            ProfileSectionTitle("Preferencias")
            ProfileListItem(
                icon = Icons.Default.Notifications,
                title = "Notificaciones",
                subtitle = "Configura alertas de riego y clima"
            )
            
            ProfileSectionTitle("Acerca de")
            ProfileListItem(
                icon = Icons.Default.Description,
                title = "Términos de Servicio",
                subtitle = "Lee nuestros términos y privacidad"
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileListItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(text = subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ir",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PlantHappyFlowerCard(
    soilMoisture: Float,
    isWaterLow: Boolean,
    temperature: Float,
    lightLevel: Float,
    plantName: String,
    onWaterClick: () -> Unit
) {
    val isNight = lightLevel < 150f
    val emotionText = when {
        isNight -> "🌙 Durmiendo plácidamente de noche..."
        soilMoisture < 20f -> "¡Me estoy secando y marchitando! 🥀"
        soilMoisture < 30f -> "¡Tengo mucha sed, riégame!"
        temperature > 32f -> "¡Hace demasiado calor aquí! ☀️"
        temperature < 15f -> "¡Brrr, tengo mucho frío! ❄️"
        soilMoisture > 80f -> "¡Floreciendo feliz y radiante! 🌸"
        else -> "¡Me siento genial y floreciendo!"
    }

    val containerColor = if (isNight) Color(0xFF1E293B)
    else if (isWaterLow) MaterialTheme.colorScheme.errorContainer
    else MaterialTheme.colorScheme.primaryContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = if (isNight) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plantName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = emotionText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isNight) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = onWaterClick
                ) {
                    Icon(Icons.Default.WaterDrop, contentDescription = "Regar", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Regar")
                }
            }

            // Hand-Drawn Blooming Flower Canvas Illustration
            HandDrawnFlowerCanvas(
                soilMoisture = soilMoisture,
                temperature = temperature,
                lightLevel = lightLevel
            )

            // Soil Moisture Progress
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Nivel de agua en tierra", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (isNight) Color.White else MaterialTheme.colorScheme.onSurface)
                    Text("${soilMoisture.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isNight) Color.White else MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { soilMoisture / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = if (isWaterLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}

@Composable
fun HandDrawnFlowerCanvas(
    soilMoisture: Float,
    temperature: Float,
    lightLevel: Float
) {
    val isThirsty = soilMoisture < 30f
    val isVeryDry = soilMoisture < 15f
    val isHot = temperature > 32f
    val isNight = lightLevel < 150f

    // Colors
    val potColor = Color(0xFFD35400) // Terracotta pot
    val potRimColor = Color(0xFFE67E22)
    val stemColor = Color(0xFF27AE60)
    val healthyPetalColor = if (isNight) Color(0xFF9B59B6) else Color(0xFFFF6B6B)
    val thirstyPetalColor = Color(0xFF95A5A6)
    val centerColor = if (isNight) Color(0xFFF1C40F) else Color(0xFFFFB703)

    val infiniteTransition = rememberInfiniteTransition(label = "flowerAnim")
    val sway by infiniteTransition.animateFloat(
        initialValue = if (isNight) -2f else -5f,
        targetValue = if (isNight) 2f else 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isNight) 3000 else 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    Box(
        modifier = Modifier
            .size(150.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            // 1. Draw Pot
            val potWidth = 64f
            val potHeight = 46f
            val potTopY = centerY + 18f
            val potBottomY = potTopY + potHeight
            val rimWidth = 78f
            val rimHeight = 12f

            val potPath = Path().apply {
                moveTo(centerX - potWidth / 2f, potTopY)
                lineTo(centerX + potWidth / 2f, potTopY)
                lineTo(centerX + potWidth / 3f, potBottomY)
                lineTo(centerX - potWidth / 3f, potBottomY)
                close()
            }
            drawPath(potPath, color = potColor)

            drawRoundRect(
                color = potRimColor,
                topLeft = Offset(centerX - rimWidth / 2f, potTopY - rimHeight / 2f),
                size = Size(rimWidth, rimHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )

            // 2. Draw Stem & Leaves
            val stemBend = if (isThirsty) 25f + sway else sway
            val leafColor = if (isThirsty) Color(0xFF7F8C8D) else Color(0xFF2ECC71)

            val stemPath = Path().apply {
                moveTo(centerX, potTopY)
                quadraticTo(centerX + stemBend, potTopY - 35f, centerX + (stemBend * 1.5f), potTopY - 70f)
            }
            drawPath(
                stemPath,
                color = stemColor,
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )

            drawArc(
                color = leafColor,
                startAngle = 180f + stemBend,
                sweepAngle = 80f,
                useCenter = true,
                topLeft = Offset(centerX - 35f, potTopY - 30f),
                size = Size(30f, 45f)
            )

            drawArc(
                color = leafColor,
                startAngle = 280f - stemBend,
                sweepAngle = 80f,
                useCenter = true,
                topLeft = Offset(centerX + 5f, potTopY - 40f),
                size = Size(30f, 45f)
            )

            // 3. Draw Flower Head
            val flowerX = centerX + (stemBend * 1.5f)
            val flowerY = potTopY - 70f
            val petalColor = if (isThirsty) thirstyPetalColor else healthyPetalColor

            val petalRadius = 14f
            val petalDist = 18f
            for (i in 0 until 5) {
                val angle = i * (360f / 5f) + (if (isThirsty) 15f else 0f)
                val rad = Math.toRadians(angle.toDouble())
                val petalX = flowerX + (petalDist * kotlin.math.cos(rad)).toFloat()
                val petalY = flowerY + (petalDist * kotlin.math.sin(rad)).toFloat()
                drawCircle(
                    color = petalColor,
                    radius = petalRadius,
                    center = Offset(petalX, petalY)
                )
            }

            drawCircle(
                color = centerColor,
                radius = 16f,
                center = Offset(flowerX, flowerY)
            )

            // 4. Face on Flower Center
            if (isNight) {
                drawArc(
                    color = Color.Black,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(flowerX - 9f, flowerY - 4f),
                    size = Size(6f, 6f),
                    style = Stroke(width = 2f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color.Black,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(flowerX + 3f, flowerY - 4f),
                    size = Size(6f, 6f),
                    style = Stroke(width = 2f, cap = StrokeCap.Round)
                )
                val sleepMouth = Path().apply {
                    moveTo(flowerX - 3f, flowerY + 5f)
                    lineTo(flowerX + 3f, flowerY + 5f)
                }
                drawPath(sleepMouth, color = Color.Black, style = Stroke(width = 2f, cap = StrokeCap.Round))
            } else {
                drawCircle(color = Color.Black, radius = 2.5f, center = Offset(flowerX - 5f, flowerY - 2f))
                drawCircle(color = Color.Black, radius = 2.5f, center = Offset(flowerX + 5f, flowerY - 2f))

                val mouthPath = Path().apply {
                    if (isThirsty) {
                        moveTo(flowerX - 5f, flowerY + 6f)
                        quadraticTo(flowerX, flowerY + 1f, flowerX + 5f, flowerY + 6f)
                    } else if (isHot) {
                        addOval(androidx.compose.ui.geometry.Rect(flowerX - 2.5f, flowerY + 2f, flowerX + 2.5f, flowerY + 8f))
                    } else {
                        moveTo(flowerX - 5f, flowerY + 3f)
                        quadraticTo(flowerX, flowerY + 8f, flowerX + 5f, flowerY + 3f)
                    }
                }
                drawPath(
                    mouthPath,
                    color = Color.Black,
                    style = Stroke(width = 2f, cap = StrokeCap.Round)
                )
            }

            if (isVeryDry) {
                drawCircle(color = Color(0xFF3498DB), radius = 3f, center = Offset(flowerX + 12f, flowerY + 2f))
            }
        }
    }
}

@Composable
fun SensorCard(
    title: String,
    value: String,
    icon: ImageVector,
    isWarning: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isWarning) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
