package com.deenshield.blocker.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import android.app.AlertDialog
import android.view.WindowManager
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import com.deenshield.blocker.data.UserPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AccessibilityBlocker : AccessibilityService() {

    @Volatile var blockedApps: Set<String> = emptySet()
    private val scope = CoroutineScope(Dispatchers.Main)
    private var prefsJob: Job? = null
    private var lastBlockedTime = 0L
    private val blockCooldownMs = 2000L // 2 seconds cooldown to prevent spam

    internal fun shouldRedirectForPackage(pkg: String): Boolean = blockedApps.contains(pkg)

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOWS_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 50
            flags = flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }

        // Observe blocked apps from DataStore
        prefsJob?.cancel()
        prefsJob = scope.launch {
            UserPrefs.blockedAppsFlow(this@AccessibilityBlocker).collectLatest { set ->
                blockedApps = set
                android.util.Log.d("AccessibilityBlocker", "Updated blocked apps: ${set.size} apps")
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        
        // Check if this package is in the blocked list
        if (pkg in blockedApps) {
            val currentTime = System.currentTimeMillis()
            
            // Prevent spam blocking
            if (currentTime - lastBlockedTime < blockCooldownMs) {
                return
            }
            lastBlockedTime = currentTime
            
            android.util.Log.d("AccessibilityBlocker", "Blocking app: $pkg")
            
            // Show blocking toast
            Toast.makeText(this, "App blocked by DeenShield", Toast.LENGTH_SHORT).show()
            
            // Redirect user to home
            val i = Intent(Intent.ACTION_MAIN)
            i.addCategory(Intent.CATEGORY_HOME)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        }
    }

    override fun onInterrupt() { /* no-op */ }

    override fun onDestroy() {
        super.onDestroy()
        prefsJob?.cancel(); prefsJob = null
    }
}
