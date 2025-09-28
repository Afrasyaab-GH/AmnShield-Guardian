package com.deenshield.blocker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BlockerApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Channel for blocking notifications
        val blockingChannel = NotificationChannel(
            BLOCKING_CHANNEL_ID,
            "Content Blocking",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications when content is blocked"
            setShowBadge(false)
        }

        // Channel for threat alerts
        val alertChannel = NotificationChannel(
            ALERT_CHANNEL_ID,
            "Security Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Important security and safety alerts"
            setShowBadge(true)
        }

        // Channel for service status
        val serviceChannel = NotificationChannel(
            SERVICE_CHANNEL_ID,
            "Service Status",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Background service status updates"
            setShowBadge(false)
        }

        // Channel for location updates
        val locationChannel = NotificationChannel(
            LOCATION_CHANNEL_ID,
            "Location Updates",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Location tracking and check-ins"
            setShowBadge(false)
        }

        notificationManager.createNotificationChannels(
            listOf(blockingChannel, alertChannel, serviceChannel, locationChannel)
        )
    }

    companion object {
        const val BLOCKING_CHANNEL_ID = "blocking_notifications"
        const val ALERT_CHANNEL_ID = "security_alerts"
        const val SERVICE_CHANNEL_ID = "service_status"
        const val LOCATION_CHANNEL_ID = "location_updates"
    }
}