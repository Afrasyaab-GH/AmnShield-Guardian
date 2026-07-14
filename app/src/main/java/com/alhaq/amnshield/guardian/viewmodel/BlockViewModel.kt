package com.alhaq.amnshield.guardian.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alhaq.amnshield.guardian.model.Block
import com.alhaq.amnshield.guardian.model.AppRule
import com.alhaq.amnshield.guardian.data.BlockRepository
import com.alhaq.amnshield.guardian.data.UserPrefs
import com.alhaq.amnshield.guardian.data.AppRuleRepository
import com.alhaq.amnshield.guardian.network.AmnShieldConnectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

@HiltViewModel
class BlockViewModel @Inject constructor(
    val connectionManager: AmnShieldConnectionManager
) : ViewModel() {
    private val _blocks = mutableListOf<Block>()
    val blocks: List<Block> get() = _blocks
    
    private val _appRules = mutableListOf<AppRule>()
    val appRules: List<AppRule> get() = _appRules

    // Predefined toggles
    var blockHarmfulKeywords by mutableStateOf(false)
        private set
    var blockHarmfulWebsites by mutableStateOf(false)
        private set
    var blockSocialMedia by mutableStateOf(false)
        private set

    var globalEnabled by mutableStateOf(true)
        private set

    var accessBetaEnabled by mutableStateOf(false)
        private set
    
    var isApiGranted by mutableStateOf(false)
        private set

    private var repo: BlockRepository? = null
    // Use AtomicReference for thread-safe context initialization
    private val contextRef = AtomicReference<Context?>(null)
    private var prefsBound = false
    
    private val context: Context? 
        get() = contextRef.get()

    fun initRepository(repository: BlockRepository) {
        if (repo != null) return
        repo = repository
        viewModelScope.launch {
            val loaded = repo!!.loadBlocks()
            _blocks.clear()
            _blocks.addAll(loaded)
            updateVpnServiceConfig()
        }
    }

    fun setContext(ctx: Context) {
        contextRef.set(ctx)
        if (!prefsBound) {
            prefsBound = true
            viewModelScope.launch {
                UserPrefs.accessBetaFlow(ctx).collectLatest { enabled ->
                    accessBetaEnabled = enabled
                }
            }
            // Periodically check API grant status
            viewModelScope.launch {
                while (true) {
                    isApiGranted = connectionManager.isGranted()
                    kotlinx.coroutines.delay(2000)
                }
            }
        }
        // Load app rules on context set
        loadAppRules()
        // Retry config update now that context is available
        updateVpnServiceConfig()
    }
    
    fun loadAppRules() {
        val ctx = context ?: return
        viewModelScope.launch {
            val apps = AppRuleRepository.loadInstalledApps(ctx)
            _appRules.clear()
            _appRules.addAll(apps)
        }
    }
    
    fun updateAppRule(rule: AppRule) {
        val ctx = context ?: return
        viewModelScope.launch {
            AppRuleRepository.updateRule(ctx, rule)
            // Update the list
            val index = _appRules.indexOfFirst { it.packageName == rule.packageName }
            if (index >= 0) {
                _appRules[index] = rule
            }
            // Push updated config
            updateVpnServiceConfig()
        }
    }

    fun updateBlockHarmfulKeywords(v: Boolean) { 
        blockHarmfulKeywords = v
        updateVpnServiceConfig()
    }
    
    fun updateBlockHarmfulWebsites(v: Boolean) { 
        blockHarmfulWebsites = v
        updateVpnServiceConfig()
    }
    
    fun updateBlockSocialMedia(v: Boolean) { 
        blockSocialMedia = v
        updateVpnServiceConfig()
    }
    
    fun updateGlobalEnabled(v: Boolean) { 
        globalEnabled = v
        updateVpnServiceConfig()
    }

    fun updateAccessBetaEnabled(enabled: Boolean) {
        accessBetaEnabled = enabled
        val ctx = context ?: return
        viewModelScope.launch {
            UserPrefs.setAccessBeta(ctx, enabled)
        }
    }

    fun addBlock(b: Block) {
        _blocks.add(0, b)
        repo?.let { r ->
            viewModelScope.launch { 
                r.saveBlock(b)
                updateVpnServiceConfig()
            }
        }
    }

    private fun updateVpnServiceConfig() {
        viewModelScope.launch {
            if (connectionManager.isConnected.value) {
                // Update keyword blocker
                connectionManager.execute("SET_KEYWORD_BLOCKER", Bundle().apply {
                    putBoolean("enable", blockHarmfulKeywords)
                })
                // Update reels/social media blocker
                connectionManager.execute("SET_REEL_BLOCKER", Bundle().apply {
                    putBoolean("enable", blockSocialMedia)
                })
                connectionManager.execute("SET_SOCIAL_MEDIA_BLOCKER", Bundle().apply {
                    putBoolean("enable", blockSocialMedia)
                })
                // Update DND/Global blocker state
                connectionManager.execute("SET_DND", Bundle().apply {
                    putBoolean("enable", globalEnabled)
                })
            }
        }
    }

    fun checkApiPermission() {
        viewModelScope.launch {
            isApiGranted = connectionManager.isGranted()
        }
    }

    fun requestApiPermission(): Intent? {
        return connectionManager.getPermissionIntent()
    }
    
    fun reconnectApi() {
        connectionManager.resolveAndBind()
    }
}
