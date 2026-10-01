package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapplication.model.PlantState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import com.example.myapplication.notification.PlantNotificationManager

class PlantViewModel(private val notificationManager: PlantNotificationManager? = null) : ViewModel() {
    private val _uiState = MutableStateFlow(PlantState())
    val uiState: StateFlow<PlantState> = _uiState.asStateFlow()

    private var lastEmotionReported: PlantNotificationManager.EmotionState? = null

    init {
        // Initial check sin emitir notificación
        updateSoilMoistureInternal(45f)
    }

    private fun updateSoilMoistureInternal(value: Float) {
        _uiState.update { current ->
            val isLow = value < 30f
            current.copy(
                soilMoisture = value,
                isWaterLow = isLow,
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
        updateSoilMoisture(85f)
        // Forzamos notificacion de felicidad al regar
        notificationManager?.showPlantEmotionNotification(PlantNotificationManager.EmotionState.HAPPY_BLOOMING)
        lastEmotionReported = PlantNotificationManager.EmotionState.HAPPY_BLOOMING
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
