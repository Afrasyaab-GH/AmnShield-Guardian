package com.alhaq.amnshield.guardian.network

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.alhaq.amnshield.api.IAmnShieldApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AmnShieldConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val actionBind = "com.alhaq.amnshield.api.BIND"
    private val actionRequestPermission = "com.alhaq.amnshield.api.REQUEST_PERMISSION"

    private val _api = MutableStateFlow<IAmnShieldApi?>(null)
    val api: StateFlow<IAmnShieldApi?> = _api.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _amnShieldPackage = MutableStateFlow<String?>(null)
    val amnShieldPackage: StateFlow<String?> = _amnShieldPackage.asStateFlow()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "AmnShield API Service Connected")
            _api.value = IAmnShieldApi.Stub.asInterface(service)
            _isConnected.value = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "AmnShield API Service Disconnected")
            _api.value = null
            _isConnected.value = false
        }
    }

    init {
        resolveAndBind()
    }

    fun resolveAmnShieldPackage(): String? {
        val currentPackage = _amnShieldPackage.value
        if (currentPackage != null) return currentPackage

        val info = context.packageManager.resolveService(Intent(actionBind), 0)
        val resolvedPackage = info?.serviceInfo?.packageName
        if (resolvedPackage != null) {
            _amnShieldPackage.value = resolvedPackage
            Log.d(TAG, "Resolved AmnShield package: $resolvedPackage")
        } else {
            Log.w(TAG, "Failed to resolve AmnShield API package")
        }
        return resolvedPackage
    }

    fun resolveAndBind(): Boolean {
        if (_isConnected.value) return true
        val pkg = resolveAmnShieldPackage() ?: return false
        val intent = Intent(actionBind).setPackage(pkg)
        return try {
            val success = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            Log.d(TAG, "bindService requested for $pkg, success=$success")
            success
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind to AmnShield service", e)
            false
        }
    }

    fun disconnect() {
        if (_isConnected.value) {
            try {
                context.unbindService(connection)
            } catch (e: Exception) {
                Log.e(TAG, "Error unbinding service", e)
            }
            _api.value = null
            _isConnected.value = false
        }
    }

    fun isGranted(): Boolean {
        val curApi = _api.value ?: return false
        return try {
            curApi.isGranted
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permission status", e)
            false
        }
    }

    fun getApiVersion(): Int {
        val curApi = _api.value ?: return 0
        return try {
            curApi.apiVersion()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching api version", e)
            0
        }
    }

    fun execute(command: String, args: Bundle = Bundle()): String {
        val curApi = _api.value ?: return "DISCONNECTED"
        return try {
            curApi.execute(command, args) ?: "FAILED"
        } catch (e: Exception) {
            Log.e(TAG, "Error executing command $command", e)
            "FAILED"
        }
    }

    fun query(state: String): String? {
        val curApi = _api.value ?: return null
        return try {
            curApi.query(state)
        } catch (e: Exception) {
            Log.e(TAG, "Error querying state $state", e)
            null
        }
    }

    fun list(kind: String): String? {
        val curApi = _api.value ?: return null
        return try {
            curApi.list(kind)
        } catch (e: Exception) {
            Log.e(TAG, "Error listing kind $kind", e)
            null
        }
    }

    fun getPermissionIntent(): Intent? {
        val pkg = resolveAmnShieldPackage() ?: return null
        return Intent(actionRequestPermission).setPackage(pkg)
    }

    companion object {
        private const val TAG = "AmnShieldConnManager"
    }
}
