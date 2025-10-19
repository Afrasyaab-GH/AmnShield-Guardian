package com.deenshield.blocker.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deenshield.blocker.model.Block
import com.deenshield.blocker.data.BlockRepository
import com.deenshield.blocker.service.BlockingVpnService
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class BlockViewModel : ViewModel() {
    private val _blocks = mutableListOf<Block>()
    val blocks: List<Block> get() = _blocks

    // Predefined toggles
    var blockHarmfulKeywords by mutableStateOf(false)
        private set
    var blockHarmfulWebsites by mutableStateOf(false)
        private set
    var blockSocialMedia by mutableStateOf(false)
        private set

    var globalEnabled by mutableStateOf(true)
        private set

    private var repo: BlockRepository? = null
    private var context: Context? = null

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
        context = ctx
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
        val ctx = context ?: return
        
        // Collect all blocked domains from blocks
        val allDomains = _blocks.flatMap { it.websites }.toSet()
        
        // Send configuration update to VPN service
        val intent = Intent(ctx, BlockingVpnService::class.java).apply {
            action = BlockingVpnService.ACTION_UPDATE_CONFIG
            putExtra(BlockingVpnService.EXTRA_BLOCKED_DOMAINS, allDomains.toTypedArray())
            putExtra(BlockingVpnService.EXTRA_BLOCK_SOCIAL_MEDIA, blockSocialMedia)
            putExtra(BlockingVpnService.EXTRA_BLOCK_ADULT_CONTENT, blockHarmfulWebsites)
            putExtra(BlockingVpnService.EXTRA_BLOCK_GAMBLING, false)
        }
        
        try {
            ctx.startService(intent)
        } catch (e: Exception) {
            android.util.Log.e("BlockViewModel", "Failed to update VPN config", e)
        }
    }
}
