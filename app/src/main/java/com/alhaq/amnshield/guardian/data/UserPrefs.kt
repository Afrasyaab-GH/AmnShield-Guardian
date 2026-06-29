package com.alhaq.amnshield.guardian.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userDataStore by preferencesDataStore(name = "user_settings")

object UserPrefs {
    private val KEY_BLOCKED_APPS: Preferences.Key<Set<String>> = stringSetPreferencesKey("blocked_apps")
    private val KEY_ACCESS_BETA: Preferences.Key<Boolean> = booleanPreferencesKey("access_beta_enabled")

    fun blockedAppsFlow(context: Context): Flow<Set<String>> =
        context.userDataStore.data.map { it[KEY_BLOCKED_APPS] ?: emptySet() }

    fun accessBetaFlow(context: Context): Flow<Boolean> =
        context.userDataStore.data.map { it[KEY_ACCESS_BETA] ?: false }

    suspend fun setBlockedApps(context: Context, packages: Set<String>) {
        context.userDataStore.edit { prefs ->
            prefs[KEY_BLOCKED_APPS] = packages
        }
    }

    suspend fun addBlockedApps(context: Context, packages: Set<String>) {
        context.userDataStore.edit { prefs ->
            val current = prefs[KEY_BLOCKED_APPS] ?: emptySet()
            prefs[KEY_BLOCKED_APPS] = current + packages
        }
    }

    suspend fun removeBlockedApp(context: Context, pkg: String) {
        context.userDataStore.edit { prefs ->
            val current = prefs[KEY_BLOCKED_APPS] ?: emptySet()
            prefs[KEY_BLOCKED_APPS] = current - pkg
        }
    }

    suspend fun setAccessBeta(context: Context, enabled: Boolean) {
        context.userDataStore.edit { prefs ->
            prefs[KEY_ACCESS_BETA] = enabled
        }
    }
}

