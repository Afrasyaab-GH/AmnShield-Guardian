package com.alhaq.amnshield.guardian.network

import com.alhaq.amnshield.guardian.util.BlockLists
import com.alhaq.amnshield.guardian.util.BlockUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentFilter @Inject constructor() {
    private val _blockEvents = MutableSharedFlow<BlockEvent>()
    val blockEvents: Flow<BlockEvent> = _blockEvents
    
    private val scope = CoroutineScope(Dispatchers.IO)
    
    // Thread-safe configuration storage using @Volatile
    @Volatile
    private var _blockedDomains: Set<String> = BlockLists.harmfulWebsites
    
    @Volatile
    private var _blockedKeywords: Set<String> = BlockLists.harmfulKeywords
    
    @Volatile
    private var _blockSocialMedia: Boolean = false
    
    @Volatile
    private var _blockAdultContent: Boolean = true
    
    @Volatile
    private var _blockGambling: Boolean = true
    
    // Public read-only accessors
    val blockedDomains: Set<String>
        get() = _blockedDomains
    
    val blockedKeywords: Set<String>
        get() = _blockedKeywords
    
    val blockSocialMedia: Boolean
        get() = _blockSocialMedia
    
    val blockAdultContent: Boolean
        get() = _blockAdultContent
    
    val blockGambling: Boolean
        get() = _blockGambling
    
    /**
     * Thread-safe method to update configuration
     * All fields are updated atomically
     */
    @Synchronized
    fun updateConfiguration(
        domains: Set<String> = _blockedDomains,
        keywords: Set<String> = _blockedKeywords,
        socialMedia: Boolean = _blockSocialMedia,
        adultContent: Boolean = _blockAdultContent,
        gambling: Boolean = _blockGambling
    ) {
        _blockedDomains = domains
        _blockedKeywords = keywords
        _blockSocialMedia = socialMedia
        _blockAdultContent = adultContent
        _blockGambling = gambling
        android.util.Log.d("ContentFilter", "Configuration updated: ${domains.size} domains, ${keywords.size} keywords")
    }
    
    /**
     * Analyze DNS query for blocking
     */
    fun shouldBlockDns(query: DnsQuery): BlockResult {
        val domain = query.domain.lowercase()
        
        // Check explicit domain blocks
        if (BlockUtils.matchDomain(blockedDomains, domain)) {
            return BlockResult(
                shouldBlock = true,
                reason = "Blocked domain: $domain",
                category = "Explicit Block"
            )
        }
        
        // Check social media
        if (blockSocialMedia && BlockUtils.matchDomain(BlockLists.socialMediaDomains, domain)) {
            return BlockResult(
                shouldBlock = true,
                reason = "Social media blocked: $domain",
                category = "Social Media"
            )
        }
        
        // Check adult content patterns
        if (blockAdultContent && isAdultContent(domain)) {
            return BlockResult(
                shouldBlock = true,
                reason = "Adult content blocked: $domain",
                category = "Adult Content"
            )
        }
        
        // Check gambling
        if (blockGambling && isGamblingContent(domain)) {
            return BlockResult(
                shouldBlock = true,
                reason = "Gambling site blocked: $domain",
                category = "Gambling"
            )
        }
        
        return BlockResult(shouldBlock = false)
    }
    
    /**
     * Analyze HTTP request for blocking
     */
    fun shouldBlockHttp(request: HttpRequest): BlockResult {
        val url = request.url.lowercase()
        val host = request.host.lowercase()
        
        // Check domain blocking first
        val dnsResult = shouldBlockDns(DnsQuery(host, "A"))
        if (dnsResult.shouldBlock) {
            return dnsResult
        }
        
        // Check URL content
        if (BlockUtils.matchKeywords(blockedKeywords, url)) {
            return BlockResult(
                shouldBlock = true,
                reason = "Harmful keywords detected in URL",
                category = "Content Filter"
            )
        }
        
        // Check user agent for suspicious patterns
        if (isSuspiciousUserAgent(request.userAgent)) {
            return BlockResult(
                shouldBlock = true,
                reason = "Suspicious user agent detected",
                category = "Security"
            )
        }
        
        return BlockResult(shouldBlock = false)
    }
    
    /**
     * Process network packet and emit block events
     */
    fun processPacket(packet: IpPacket) {
        scope.launch {
            try {
                val parser = PacketParser()
                
                // Handle DNS queries
                val dnsQuery = parser.parseDnsQuery(packet)
                if (dnsQuery != null) {
                    val result = shouldBlockDns(dnsQuery)
                    if (result.shouldBlock) {
                        _blockEvents.emit(
                            BlockEvent(
                                type = BlockEvent.Type.DNS,
                                domain = dnsQuery.domain,
                                reason = result.reason,
                                category = result.category,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
                
                // Handle HTTP requests
                val httpRequest = parser.parseHttpRequest(packet)
                if (httpRequest != null) {
                    val result = shouldBlockHttp(httpRequest)
                    if (result.shouldBlock) {
                        _blockEvents.emit(
                            BlockEvent(
                                type = BlockEvent.Type.HTTP,
                                domain = httpRequest.host,
                                url = httpRequest.url,
                                reason = result.reason,
                                category = result.category,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
                
            } catch (e: Exception) {
                // Log error but continue processing
                android.util.Log.w("ContentFilter", "Error processing packet", e)
            }
        }
    }
    
    /**
     * Update blocking configuration with nullable parameters
     */
    fun updateConfigurationNullable(
        domains: Set<String>? = null,
        keywords: Set<String>? = null,
        socialMedia: Boolean? = null,
        adultContent: Boolean? = null,
        gambling: Boolean? = null
    ) {
        updateConfiguration(
            domains = domains ?: _blockedDomains,
            keywords = keywords ?: _blockedKeywords,
            socialMedia = socialMedia ?: _blockSocialMedia,
            adultContent = adultContent ?: _blockAdultContent,
            gambling = gambling ?: _blockGambling
        )
    }
    
    private fun isAdultContent(domain: String): Boolean {
        val adultPatterns = setOf(
            "porn", "xxx", "adult", "sex", "nude", "naked", "erotic"
        )
        return adultPatterns.any { pattern -> domain.contains(pattern) }
    }
    
    private fun isGamblingContent(domain: String): Boolean {
        val gamblingPatterns = setOf(
            "casino", "bet", "poker", "slots", "gambling", "lottery"
        )
        return gamblingPatterns.any { pattern -> domain.contains(pattern) }
    }
    
    private fun isSuspiciousUserAgent(userAgent: String): Boolean {
        val suspiciousPatterns = setOf(
            "crawler", "bot", "spider", "scraper"
        )
        val ua = userAgent.lowercase()
        return suspiciousPatterns.any { pattern -> ua.contains(pattern) }
    }
}

/**
 * Result of content filtering analysis
 */
data class BlockResult(
    val shouldBlock: Boolean,
    val reason: String = "",
    val category: String = "",
    val confidence: Float = 1.0f
)

/**
 * Block event for logging and notifications
 */
data class BlockEvent(
    val type: Type,
    val domain: String = "",
    val url: String = "",
    val reason: String,
    val category: String,
    val timestamp: Long,
    val appPackage: String = ""
) {
    enum class Type {
        DNS, HTTP, APP, KEYWORD
    }
}
