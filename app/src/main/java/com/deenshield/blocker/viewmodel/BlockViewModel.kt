package com.deenshield.blocker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deenshield.blocker.model.Block
import com.deenshield.blocker.data.BlockRepository
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

    fun initRepository(repository: BlockRepository) {
        if (repo != null) return
        repo = repository
        viewModelScope.launch {
            val loaded = repo!!.loadBlocks()
            _blocks.clear()
            _blocks.addAll(loaded)
        }
    }

    fun setBlockHarmfulKeywords(v: Boolean) { blockHarmfulKeywords = v }
    fun setBlockHarmfulWebsites(v: Boolean) { blockHarmfulWebsites = v }
    fun setBlockSocialMedia(v: Boolean) { blockSocialMedia = v }
    fun setGlobalEnabled(v: Boolean) { globalEnabled = v }

    fun addBlock(b: Block) {
        _blocks.add(0, b)
        repo?.let { r ->
            viewModelScope.launch { r.saveBlock(b) }
        }
    }
}
