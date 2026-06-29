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
import neth.iecal.curbox.api.ICurboxApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AmnShieldConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val actionBind = "neth.iecal.curbox.api.BIND"
    private val actionRequestPermission = "neth.iecal.curbox.api.REQUEST_PERMISSION"

    private val _api = MutableStateFlow<ICurboxApi?>(null)
    val api: StateFlow<ICurboxApi?> = _api.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _curboxPackage = MutableStateFlow<String?>(null)
    val curboxPackage: StateFlow<String?> = _curboxPackage.asStateFlow()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Curbox API Service Connected")
            _api.value = ICurboxApi.Stub.asInterface(service)
            _isConnected.value = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Curbox API Service Disconnected")
            _api.value = null
            _isConnected.value = false
        }
    }

    init {
        resolveAndBind()
    }

    fun resolveCurboxPackage(): String? {
        val currentPackage = _curboxPackage.value
        if (currentPackage != null) return currentPackage

        val info = context.packageManager.resolveService(Intent(actionBind), 0)
        val resolvedPackage = info?.serviceInfo?.packageName
        if (resolvedPackage != null) {
            _curboxPackage.value = resolvedPackage
            Log.d(TAG, "Resolved Curbox package: $resolvedPackage")
        } else {
            Log.w(TAG, "Failed to resolve Curbox API package")
        }
        return resolvedPackage
    }

    fun resolveAndBind(): Boolean {
        if (_isConnected.value) return true
        val pkg = resolveCurboxPackage() ?: return false
        val intent = Intent(actionBind).setPackage(pkg)
        return try {
            val success = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            Log.d(TAG, "bindService requested for $pkg, success=$success")
            success
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind to Curbox service", e)
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
        val pkg = resolveCurboxPackage() ?: return null
        return Intent(actionRequestPermission).setPackage(pkg)
    }

    companion object {
        private const val TAG = "AmnShieldConnManager"
    }
}
