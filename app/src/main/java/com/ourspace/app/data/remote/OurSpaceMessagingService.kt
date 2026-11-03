package com.ourspace.app.data.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ourspace.app.data.local.preferences.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OurSpaceMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val eventType = data["eventType"] ?: "GENERAL"
        if (eventType.startsWith("PRIVATE_")) return

        serviceScope.launch {
            val prefs = preferencesRepository.preferencesFlow.first()
            val isAllowed = when (eventType) {
                "SHARED_NOTE" -> prefs.notifySharedNotes
                "LOVE_NOTE" -> prefs.notifyLoveNotes
                "NEW_MEMORY" -> prefs.notifyNewMemories
                "ANNIVERSARY_REMINDER" -> prefs.notifyAnniversaryReminders
                "CALENDAR_EVENT" -> prefs.notifyCalendarEvents
                "SURPRISE" -> prefs.notifySurprises
                else -> true
            }
            if (!isAllowed) return@launch

            val safeTitle = data["title"] ?: "Our Space"
            val safeBody = when (eventType) {
                "SHARED_NOTE" -> "${prefs.cachedPartnerNickname} memperbarui catatan bersama di Our Space."
                "LOVE_NOTE" -> "${prefs.cachedPartnerNickname} mengirimkan Love Note baru untukmu 💌"
                "NEW_MEMORY" -> "${prefs.cachedPartnerNickname} menambahkan kenangan baru di Memories 📸"
                "SURPRISE" -> "Ada kejutan spesial yang sudah terbuka untukmu di Our Space ✨"
                else -> data["body"] ?: "Ketuk untuk membuka Our Space."
            }
            showNotification(safeTitle, safeBody)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "our_space_couple_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Our Space Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
