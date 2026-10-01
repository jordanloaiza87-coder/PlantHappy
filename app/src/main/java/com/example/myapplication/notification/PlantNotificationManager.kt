package com.example.myapplication.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R

class PlantNotificationManager(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Plant Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas sobre el cuidado y emociones de tus plantas"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showPlantEmotionNotification(emotion: EmotionState) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val (title, text) = when (emotion) {
            EmotionState.THIRSTY -> Pair(
                "¡Tengo sed! \uD83E\uDD7A", 
                "El nivel de agua en la tierra ha bajado del 30%. Necesito que me riegues pronto."
            )
            EmotionState.DRY_DYING -> Pair(
                "¡Me estoy secando! \uD83E\uDD40", 
                "El nivel de agua es crítico (menos de 20%). Por favor, ¡dame agua o me marchitaré!"
            )
            EmotionState.HAPPY_BLOOMING -> Pair(
                "¡Qué feliz estoy! \uD83C\uDF38", 
                "Acabo de ser regada y estoy radiante. ¡Gracias por cuidarme!"
            )
            EmotionState.HOT -> Pair(
                "¡Qué calor hace! ☀️", 
                "La temperatura superó los 32°C. Me vendría bien un poco de sombra o humedad."
            )
            EmotionState.COLD -> Pair(
                "¡Brrr, qué frío! ❄️", 
                "La temperatura bajó de los 15°C. ¿Podrías moverme a un lugar más cálido?"
            )
            EmotionState.SLEEPING -> Pair(
                "Shhh... estoy durmiendo \uD83C\uDF19", 
                "Es de noche y me estoy tomando un descanso."
            )
            EmotionState.PERFECT -> Pair(
                "Me siento genial \uD83C\uDF3F", 
                "Mi temperatura, humedad y luz son ideales. ¡Sigue así!"
            )
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_eco)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            notificationManager.notify(emotion.ordinal, builder.build())
        } catch (e: Exception) {
            // Manejamos SecurityException (permiso no concedido) o IllegalArgumentException
            e.printStackTrace()
        }
    }

    enum class EmotionState {
        THIRSTY,
        DRY_DYING,
        HAPPY_BLOOMING,
        HOT,
        COLD,
        SLEEPING,
        PERFECT
    }

    companion object {
        private const val CHANNEL_ID = "plant_alert_channel"
    }
}