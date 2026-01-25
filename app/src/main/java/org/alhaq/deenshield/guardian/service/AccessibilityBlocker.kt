package org.alhaq.deenshield.guardian.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import org.alhaq.deenshield.guardian.data.UserPrefs
import org.alhaq.deenshield.guardian.service.Scheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * Accessibility service for blocking specific apps
 * Includes schedule checking to support time-based blocking
 */
class AccessibilityBlocker : AccessibilityService() {

    @Volatile var blockedApps: Set<String> = emptySet()
    @Volatile var blockSchedules: Map<String, Map<Int, List<IntRange>>> = emptyMap()
    
    private val scope = CoroutineScope(Dispatchers.Main)
    private var prefsJob: Job? = null
    private var lastBlockedTime = 0L
    private val blockCooldownMs = 2000L // 2 seconds cooldown to prevent spam
    private val scheduler = Scheduler()

    internal fun shouldRedirectForPackage(pkg: String): Boolean = blockedApps.contains(pkg)

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo?.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOWS_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 50
            flags = flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }

        android.util.Log.i("AccessibilityBlocker", "Service connected and configured")

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
        if (pkg !in blockedApps) {
            return
        }
        
        // Check if blocking is scheduled for now
        val schedule = blockSchedules[pkg]
        if (schedule != null && !scheduler.isActive(schedule, LocalDateTime.now())) {
            android.util.Log.d("AccessibilityBlocker", "App $pkg blocked but not in active schedule window")
            return
        }
        
        val currentTime = System.currentTimeMillis()
        
        // Prevent spam blocking
        if (currentTime - lastBlockedTime < blockCooldownMs) {
            return
        }
        lastBlockedTime = currentTime
        
        android.util.Log.d("AccessibilityBlocker", "Blocking app: $pkg (schedule-aware)")
        
        // Show blocking toast
        Toast.makeText(this, "App blocked by DeenShield", Toast.LENGTH_SHORT).show()
        
        // Redirect user to home
        val i = Intent(Intent.ACTION_MAIN)
        i.addCategory(Intent.CATEGORY_HOME)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    override fun onInterrupt() { 
        android.util.Log.w("AccessibilityBlocker", "Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        prefsJob?.cancel()
        prefsJob = null
        android.util.Log.i("AccessibilityBlocker", "Service destroyed")
    }

    companion object {
        /**
         * Check if accessibility service is actually enabled in system settings
         * @return true if service is enabled, false otherwise
         */
        fun isServiceEnabled(context: Context): Boolean {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            
            val serviceName = "${context.packageName}/${AccessibilityBlocker::class.java.name}"
            return enabledServices.contains(serviceName)
        }
    }
}
