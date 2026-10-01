package com.example.myapplication.model

data class PlantState(
    val plantName: String = "Monstera Deliciosa",
    val soilMoisture: Float = 45f, // percentage 0 - 100
    val temperature: Float = 22f, // Celsius
    val airHumidity: Float = 60f, // percentage 0 - 100
    val lightLevel: Float = 500f, // Lux
    val isWaterLow: Boolean = false,
    val environmentStatus: String = "Óptimo",
    val recommendations: List<String> = listOf("La planta está en buenas condiciones.")
)
