package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapplication.model.PlantState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import com.example.myapplication.notification.PlantNotificationManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PlantViewModel(private val notificationManager: PlantNotificationManager? = null) : ViewModel() {
    private val _uiState = MutableStateFlow(PlantState())
    val uiState: StateFlow<PlantState> = _uiState.asStateFlow()

    private var lastEmotionReported: PlantNotificationManager.EmotionState? = null
    
    // Referencia al nodo "sensores" en Firebase Realtime Database
    private val databaseRef = FirebaseDatabase.getInstance().getReference("sensores")

    init {
        // Inicializamos con un estado base
        updateSoilMoistureInternal(45f)
        
        // Empezamos a escuchar los cambios en tiempo real desde Firebase (Arduino Wemos)
        startListeningToFirebase()
    }

    private fun startListeningToFirebase() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Obtenemos los valores de la nube (con fallbacks a los actuales si algo falla)
                val current = _uiState.value
                val humedadTierra = snapshot.child("humedadTierra").getValue(Float::class.java) ?: current.soilMoisture
                val temperatura = snapshot.child("temperatura").getValue(Float::class.java) ?: current.temperature
                val humedadAmbiente = snapshot.child("humedadAmbiente").getValue(Float::class.java) ?: current.airHumidity
                val luz = snapshot.child("luz").getValue(Float::class.java) ?: current.lightLevel

                // Actualizamos todos los datos de golpe en la interfaz
                updateAllSensorsFromFirebase(humedadTierra, temperatura, humedadAmbiente, luz)
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de errores de conexión o permisos
                println("Error leyendo Firebase: ${error.message}")
            }
        })
    }
    
    private fun updateAllSensorsFromFirebase(
        humedadTierra: Float,
        temp: Float,
        humedadAmb: Float,
        luz: Float
    ) {
        _uiState.update { current ->
            if (current.isDead && humedadTierra > 0f) return@update current // Sigue muerta
            
            val isLow = humedadTierra < 30f
            val isDead = humedadTierra <= 0f
            
            val envStatus = when {
                temp < 15f -> "Demasiado Frío"
                temp > 32f -> "Demasiado Caliente"
                else -> "Óptimo"
            }

            current.copy(
                soilMoisture = humedadTierra,
                isWaterLow = isLow,
                isDead = isDead,
                temperature = temp,
                airHumidity = humedadAmb,
                lightLevel = luz,
                environmentStatus = envStatus,
                recommendations = generateRecommendations(isLow, temp, humedadAmb, luz)
            )
        }
        
        // Evaluamos si debemos lanzar una notificación push al usuario
        checkAndSendNotification()
    }

    private fun updateSoilMoistureInternal(value: Float) {
        _uiState.update { current ->
            if (current.isDead && value > 0f) return@update current // No se puede regar a medias si está muerta (solo con reset)
            val isLow = value < 30f
            val isDead = value <= 0f
            current.copy(
                soilMoisture = value,
                isWaterLow = isLow,
                isDead = isDead,
                recommendations = generateRecommendations(isLow, current.temperature, current.airHumidity, current.lightLevel)
            )
        }
    }

    fun updateSoilMoisture(value: Float) {
        updateSoilMoistureInternal(value)
        checkAndSendNotification()
    }

    fun updateTemperature(value: Float) {
        _uiState.update { current ->
            val envStatus = when {
                value < 15f -> "Demasiado Frío"
                value > 32f -> "Demasiado Caliente"
                else -> "Óptimo"
            }
            current.copy(
                temperature = value,
                environmentStatus = envStatus,
                recommendations = generateRecommendations(current.isWaterLow, value, current.airHumidity, current.lightLevel)
            )
        }
        checkAndSendNotification()
    }

    fun updateAirHumidity(value: Float) {
        _uiState.update { current ->
            current.copy(
                airHumidity = value,
                recommendations = generateRecommendations(current.isWaterLow, current.temperature, value, current.lightLevel)
            )
        }
        checkAndSendNotification()
    }

    fun updateLightLevel(value: Float) {
        _uiState.update { current ->
            current.copy(
                lightLevel = value,
                recommendations = generateRecommendations(current.isWaterLow, current.temperature, current.airHumidity, value)
            )
        }
        checkAndSendNotification()
    }

    fun waterPlant() {
        val currentState = _uiState.value
        if (currentState.isDead) return // Si está muerta no se puede regar, hay que reiniciar
        
        _uiState.update { current ->
            current.copy(
                wateringsToday = current.wateringsToday + 1,
                waterCoins = current.waterCoins + 20 // Premiar con 20 monedas por regar
            )
        }
        updateSoilMoisture(85f)
        // Forzamos notificacion de felicidad al regar
        notificationManager?.showPlantEmotionNotification(PlantNotificationManager.EmotionState.HAPPY_BLOOMING)
        lastEmotionReported = PlantNotificationManager.EmotionState.HAPPY_BLOOMING
    }

    fun buyPot(potId: String, cost: Int) {
        // En lugar de usar monedas, la compra se basa en si tienes suficientes "días felices seguidos" (happyDaysStreak)
        _uiState.update { current ->
            if (current.happyDaysStreak >= cost && !current.unlockedPots.contains(potId)) {
                current.copy(
                    unlockedPots = current.unlockedPots + listOf(potId),
                    equippedPot = potId
                )
            } else current
        }
    }

    fun equipPot(potId: String) {
        _uiState.update { current ->
            if (current.unlockedPots.contains(potId)) {
                current.copy(equippedPot = potId)
            } else current
        }
    }

    fun advanceDay() {
        _uiState.update { current ->
            if (current.isDead) return@update current
            
            // Si la humedad está bien, aumenta la racha feliz. Si no, se rompe.
            val newStreak = if (current.soilMoisture > 30f && current.temperature in 15f..32f) {
                current.happyDaysStreak + 1
            } else {
                0
            }
            
            current.copy(
                happyDaysStreak = newStreak,
                wateringsToday = 0 // Se reinician los riegos al cambiar de día
            )
        }
    }

    fun resetPlant() {
        _uiState.value = PlantState(
            soilMoisture = 45f,
            wateringsToday = 0,
            waterCoins = 0, // Se pierden las monedas
            happyDaysStreak = 0, // Se pierde la racha
            healthScore = 50, // Se resetea la salud
            historicalMoisture = listOf(0f, 0f, 0f, 0f, 0f, 0f, 45f),
            unlockedPots = listOf("terrash"), // Se pierden los cosméticos
            equippedPot = "terrash",
            isDead = false
        )
    }

    private fun checkAndSendNotification() {
        val currentState = _uiState.value
        val newEmotion = when {
            currentState.lightLevel < 150f -> PlantNotificationManager.EmotionState.SLEEPING
            currentState.soilMoisture < 20f -> PlantNotificationManager.EmotionState.DRY_DYING
            currentState.soilMoisture < 30f -> PlantNotificationManager.EmotionState.THIRSTY
            currentState.temperature > 32f -> PlantNotificationManager.EmotionState.HOT
            currentState.temperature < 15f -> PlantNotificationManager.EmotionState.COLD
            currentState.soilMoisture > 80f -> PlantNotificationManager.EmotionState.HAPPY_BLOOMING
            else -> PlantNotificationManager.EmotionState.PERFECT
        }

        if (newEmotion != lastEmotionReported && newEmotion != PlantNotificationManager.EmotionState.PERFECT) {
            notificationManager?.showPlantEmotionNotification(newEmotion)
            lastEmotionReported = newEmotion
        } else if (newEmotion == PlantNotificationManager.EmotionState.PERFECT) {
            lastEmotionReported = newEmotion
        }
    }

    private fun generateRecommendations(isWaterLow: Boolean, temp: Float, humidity: Float, light: Float): List<String> {
        val list = mutableListOf<String>()
        if (isWaterLow) {
            list.add("⚠️ ¡Atención! La tierra está seca (<30%). Es hora de regar la planta.")
        } else {
            list.add("💧 Nivel de humedad en la tierra adecuado.")
        }

        if (temp < 18f) {
            list.add("🌡️ La temperatura ambiente es baja (<18°C). Considera moverla a un lugar más cálido.")
        } else if (temp > 30f) {
            list.add("🌡️ Hace mucho calor (>30°C). Asegúrate de que no le dé sol directo intenso.")
        } else {
            list.add("🌡️ Temperatura ambiental perfecta.")
        }

        if (humidity < 40f) {
            list.add("💨 El ambiente es seco (<40%). Rociar un poco de agua en las hojas puede ayudar.")
        } else {
            list.add("💨 Humedad ambiental agradable.")
        }

        if (light < 200f) {
            list.add("☀️ Luz baja (<200 lx). Ubícala cerca de una ventana luminosa.")
        } else {
            list.add("☀️ Buena iluminación ambiental.")
        }

        return list
    }
}
