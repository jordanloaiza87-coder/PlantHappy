package com.example.myapplication.model

data class PlantState(
    val plantName: String = "Monstera Deliciosa",
    val soilMoisture: Float = 45f, // percentage 0 - 100
    val temperature: Float = 22f, // Celsius
    val airHumidity: Float = 60f, // percentage 0 - 100
    val lightLevel: Float = 500f, // Lux
    val isWaterLow: Boolean = false,
    val isDead: Boolean = false,
    val environmentStatus: String = "Óptimo",
    val recommendations: List<String> = listOf("La planta está en buenas condiciones."),
    
    // Estadísticas
    val wateringsToday: Int = 0,
    val waterCoins: Int = 0, // Monedas que se ganan regando
    val happyDaysStreak: Int = 12, // Días seguidos con buena salud
    val healthScore: Int = 98, // Porcentaje de salud general (0 - 100)
    val sunHoursToday: Float = 5.2f,
    val historicalMoisture: List<Float> = listOf(65f, 50f, 30f, 90f, 75f, 60f, 45f),
    
    // Cosméticos e Inventario
    val unlockedPots: List<String> = listOf("terrash"), // "terrash" = Maceta clásica por defecto
    val equippedPot: String = "terrash"
)
