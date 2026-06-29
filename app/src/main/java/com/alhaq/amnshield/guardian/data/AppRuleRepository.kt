package com.alhaq.amnshield.guardian.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.alhaq.amnshield.guardian.model.AppRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

/**
 * Manages per-app WiFi/internet blocking rules for DeenShield Access integration.
 * Provides loading, saving, and querying of app-level rules.
 */
object AppRuleRepository {
    private const val TAG = "AppRuleRepository"
    private val rules = mutableMapOf<String, AppRule>()
    private val prefs = "app_rules"
    
    suspend fun loadInstalledApps(context: Context): List<AppRule> = withContext(Dispatchers.Default) {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(0)
        val appRules = mutableListOf<AppRule>()
        
        packages.forEach { pkg ->
            try {
                val appInfo = pkg.applicationInfo
                if (appInfo != null) {
                    val packageName = pkg.packageName
                    val uid = appInfo.uid

                    val label = appInfo.loadLabel(pm).toString()
                    val isSystem = (appInfo.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0
                    val hasInternet = pm.checkPermission(
                        android.Manifest.permission.INTERNET,
                        packageName
                    ) == PackageManager.PERMISSION_GRANTED
                    val isEnabled = appInfo.enabled

                    // Load existing rule or create new one
                    val existingRule = rules[packageName]
                    val rule = existingRule?.copy(
                        appName = label,
                        isSystem = isSystem,
                        hasInternet = hasInternet,
                        isEnabled = isEnabled,
                        updatedAt = Instant.now()
                    ) ?: AppRule(
                        uid = uid,
                        packageName = packageName,
                        appName = label,
                        isSystem = isSystem,
                        hasInternet = hasInternet,
                        isEnabled = isEnabled
                    )

                    appRules.add(rule)
                    rules[packageName] = rule
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load app info for package: $e")
            }
        }
        
        appRules.sortedBy { it.appName }
    }
    
    suspend fun getRuleForPackage(context: Context, packageName: String): AppRule? = withContext(Dispatchers.Default) {
        if (rules[packageName] != null) return@withContext rules[packageName]
        
        val pm = context.packageManager
        return@withContext try {
            val pkg = pm.getPackageInfo(packageName, 0)
            val appInfo = pkg.applicationInfo
            if (appInfo != null) {
                val label = appInfo.loadLabel(pm).toString()
                val isSystem = (appInfo.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0
                val hasInternet = pm.checkPermission(
                    android.Manifest.permission.INTERNET,
                    packageName
                ) == PackageManager.PERMISSION_GRANTED

                AppRule(
                    uid = appInfo.uid,
                    packageName = packageName,
                    appName = label,
                    isSystem = isSystem,
                    hasInternet = hasInternet,
                    isEnabled = appInfo.enabled
                ).also { rules[packageName] = it }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get rule for $packageName: $e")
            null
        }
    }
    
    suspend fun updateRule(context: Context, rule: AppRule) = withContext(Dispatchers.IO) {
        rules[rule.packageName] = rule.copy(updatedAt = Instant.now())
        
        // Persist to shared preferences
        val sp = context.getSharedPreferences(prefs, Context.MODE_PRIVATE)
        sp.edit().apply {
            putBoolean("${rule.packageName}_wifi_blocked", rule.wifiBlocked)
            putBoolean("${rule.packageName}_mobile_blocked", rule.mobileBlocked)
            putBoolean("${rule.packageName}_allow_wifi_screen_on", rule.allowWifiWhenScreenOn)
            putBoolean("${rule.packageName}_allow_mobile_screen_on", rule.allowMobileWhenScreenOn)
            putBoolean("${rule.packageName}_roaming", rule.blockWhenRoaming)
            putBoolean("${rule.packageName}_lockdown", rule.lockdown)
            apply()
        }
        
        Log.d(TAG, "Saved rule for ${rule.packageName}: wifi_blocked=${rule.wifiBlocked}, mobile_blocked=${rule.mobileBlocked}")
    }
    
    suspend fun restoreRule(context: Context, packageName: String): AppRule? = withContext(Dispatchers.IO) {
        val sp = context.getSharedPreferences(prefs, Context.MODE_PRIVATE)
        val existing = rules[packageName] ?: getRuleForPackage(context, packageName) ?: return@withContext null
        
        return@withContext existing.copy(
            wifiBlocked = sp.getBoolean("${packageName}_wifi_blocked", false),
            mobileBlocked = sp.getBoolean("${packageName}_mobile_blocked", false),
            allowWifiWhenScreenOn = sp.getBoolean("${packageName}_allow_wifi_screen_on", false),
            allowMobileWhenScreenOn = sp.getBoolean("${packageName}_allow_mobile_screen_on", false),
            blockWhenRoaming = sp.getBoolean("${packageName}_roaming", false),
            lockdown = sp.getBoolean("${packageName}_lockdown", false)
        ).also { rules[packageName] = it }
    }
    
    fun getBlockedAppsForWifi(): Set<String> = rules.values
        .filter { it.wifiBlocked }
        .map { it.packageName }
        .toSet()
    
    fun getBlockedAppsForMobile(): Set<String> = rules.values
        .filter { it.mobileBlocked }
        .map { it.packageName }
        .toSet()
    
    fun clearCache() {
        rules.clear()
        Log.d(TAG, "Cleared app rule cache")
    }
}

