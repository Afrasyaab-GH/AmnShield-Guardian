package org.alhaq.deenshield.guardian.viewmodel

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.alhaq.deenshield.guardian.model.Block
import org.alhaq.deenshield.guardian.model.AppRule
import org.alhaq.deenshield.guardian.data.BlockRepository
import org.alhaq.deenshield.guardian.data.UserPrefs
import org.alhaq.deenshield.guardian.data.AppRuleRepository
import org.alhaq.deenshield.guardian.service.BlockingVpnService
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.concurrent.atomic.AtomicReference

class BlockViewModel : ViewModel() {
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
    
    var vpnActive by mutableStateOf(false)
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
            // Push updated config to VPN service
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
            if (enabled) {
                // Try to start VPN when beta is enabled
                val prepare = android.net.VpnService.prepare(ctx)
                if (prepare == null) {
                    val intent = Intent(ctx, BlockingVpnService::class.java).apply { 
                        action = BlockingVpnService.ACTION_START 
                    }
                    ctx.startService(intent)
                }
            }
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
        val ctx = context ?: run {
            android.util.Log.w("BlockViewModel", "Context not available yet, cannot update VPN config")
            return
        }
        
        if (!globalEnabled) {
            android.util.Log.d("BlockViewModel", "Global blocking disabled, not updating VPN config")
            return
        }
        
        // Collect all blocked domains from blocks and app rules
        val allDomains = mutableSetOf<String>()
        _blocks.forEach { block ->
            allDomains.addAll(block.websites)
        }
        
        // Get WiFi and mobile blocked apps
        val wifiBlockedApps = AppRuleRepository.getBlockedAppsForWifi()
        val mobileBlockedApps = AppRuleRepository.getBlockedAppsForMobile()
        
        // Send configuration update to VPN service
        val intent = Intent(ctx, BlockingVpnService::class.java).apply {
            action = BlockingVpnService.ACTION_UPDATE_CONFIG
            putExtra(BlockingVpnService.EXTRA_BLOCKED_DOMAINS, allDomains.toTypedArray())
            putExtra(BlockingVpnService.EXTRA_BLOCK_SOCIAL_MEDIA, blockSocialMedia)
            putExtra(BlockingVpnService.EXTRA_BLOCK_ADULT_CONTENT, blockHarmfulWebsites)
            putExtra(BlockingVpnService.EXTRA_BLOCK_KEYWORDS, blockHarmfulKeywords)
            putExtra("blocked_wifi_apps", wifiBlockedApps.toTypedArray())
            putExtra("blocked_mobile_apps", mobileBlockedApps.toTypedArray())
        }
        
        try {
            ctx.startService(intent)
            android.util.Log.d("BlockViewModel", "VPN config update sent: ${allDomains.size} domains, wifi_apps=${wifiBlockedApps.size}, mobile_apps=${mobileBlockedApps.size}")
        } catch (e: Exception) {
            android.util.Log.e("BlockViewModel", "Failed to update VPN config: ${e.message}", e)
        }
    }
}

