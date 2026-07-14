package com.alhaq.amnshield.guardian.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.alhaq.amnshield.guardian.BlockerApplication
import com.alhaq.amnshield.guardian.MainActivity
import com.alhaq.amnshield.guardian.R
import com.alhaq.amnshield.guardian.network.ContentFilter
import com.alhaq.amnshield.guardian.network.PacketParser
import com.alhaq.amnshield.guardian.network.DnsProxy
import com.alhaq.amnshield.guardian.util.BlockUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import javax.inject.Inject
import java.net.InetAddress

/**
 * Enhanced VPN Service with real-time packet inspection and content filtering
 * Provides deep packet inspection similar to NetSpark's dynamic filtering
 */
@AndroidEntryPoint
class BlockingVpnService : VpnService() {
    
    @Inject
    lateinit var contentFilter: ContentFilter
    
    private var vpnInterface: ParcelFileDescriptor? = null
    private var inputStream: FileInputStream? = null
    private var outputStream: FileOutputStream? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var processingJob: Job? = null
    
    private val packetParser = PacketParser()
    private val packetBuffer = ByteBuffer.allocate(65536) // 64KB buffer
    private val dnsProxy = DnsProxy()
    
    // Statistics
    private var packetsProcessed = 0L
    private var packetsBlocked = 0L
    private var bytesProcessed = 0L
    
    // Predefined block lists (populate from repository or preferences)
    @Volatile
    var blockedDomains: Set<String> = emptySet()
        private set
    
    @Volatile
    var isActive: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()
        
        // Initialize content filter with default block lists
        contentFilter.updateConfiguration(
            domains = com.alhaq.amnshield.guardian.util.BlockLists.harmfulWebsites,
            keywords = com.alhaq.amnshield.guardian.util.BlockLists.harmfulKeywords,
            socialMedia = false,
            adultContent = true,
            gambling = true
        )
        
        setupNotificationObserver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startVpnService()
            ACTION_STOP -> stopVpnService()
            ACTION_UPDATE_CONFIG -> updateConfiguration(intent)
        }
        return START_STICKY
    }
    
    private fun startVpnService() {
        if (isActive) return
        
        try {
            // Build TUN interface with comprehensive routing
            vpnInterface = Builder()
                .setSession("AmnShield Protection")
                .setMtu(1500)
                .addAddress("10.0.0.2", 24) // VPN interface IP
                // Route ALL traffic through VPN for proper filtering
                .addRoute("0.0.0.0", 0)
                .addDnsServer("8.8.8.8")
                .addDnsServer("8.8.4.4")
                // Allow our own package to bypass VPN to prevent loops
                .addDisallowedApplication(packageName)
                .setBlocking(false)
                .establish()
            
            vpnInterface?.let { vpn ->
                inputStream = FileInputStream(vpn.fileDescriptor)
                outputStream = FileOutputStream(vpn.fileDescriptor)
                
                isActive = true
                startForeground(NOTIFICATION_ID, createNotification())
                
                // Start packet processing
                processingJob = serviceScope.launch {
                    processPackets()
                }
                
                android.util.Log.i("BlockingVpnService", "VPN service started successfully")
            }
            
        } catch (e: Exception) {
            android.util.Log.e("BlockingVpnService", "Failed to start VPN service", e)
            stopVpnService()
        }
    }
    
    private fun stopVpnService() {
        isActive = false
        
        processingJob?.cancel()
        processingJob = null
        
        inputStream?.close()
        outputStream?.close()
        vpnInterface?.close()
        
        inputStream = null
        outputStream = null
        vpnInterface = null
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        
        android.util.Log.i("BlockingVpnService", "VPN service stopped")
    }
    
    private suspend fun processPackets() = withContext(Dispatchers.IO) {
        val input = inputStream ?: return@withContext
        val output = outputStream ?: return@withContext
        
        try {
            while (isActive && currentCoroutineContext().isActive) {
                try {
                    // Read packet from TUN interface
                    packetBuffer.clear()
                    val bytesRead = input.read(packetBuffer.array())
                    
                    if (bytesRead > 0) {
                        packetBuffer.limit(bytesRead)
                        bytesProcessed += bytesRead
                        packetsProcessed++
                        
                        // Parse IP packet
                        val packet = packetParser.parseIpPacket(packetBuffer)
                        
                        if (packet != null) {
                            // Handle DNS packets specially (UDP/53)
                            if (packet.protocol == PacketParser.PROTOCOL_UDP && packet.destinationPort == PacketParser.DNS_PORT) {
                                val dnsResponse = dnsProxy.processDnsQuery(packet.payload, blockedDomains)
                                if (dnsResponse != null) {
                                    // Send DNS response back to requester
                                    val respPacket = buildIpv4UdpResponse(packet, dnsResponse)
                                    if (respPacket != null) {
                                        output.write(respPacket)
                                        bytesProcessed += respPacket.size
                                        if (dnsResponse !== packet.payload) packetsBlocked++
                                        continue
                                    }
                                }
                            }

                            // Analyze packet for other threats
                            val shouldBlock = analyzePacket(packet)
                            
                            if (shouldBlock) {
                                packetsBlocked++
                                // Drop packet by not forwarding it
                                android.util.Log.d("BlockingVpnService", 
                                    "Blocked packet to ${packet.destinationAddress}")
                            } else {
                                // Forward packet
                                packetBuffer.rewind()
                                output.write(packetBuffer.array(), 0, bytesRead)
                            }
                        } else {
                            // Forward unknown packets
                            packetBuffer.rewind()
                            output.write(packetBuffer.array(), 0, bytesRead)
                        }
                        
                        // Update UI every 100 packets
                        if (packetsProcessed % 100 == 0L) {
                            updateNotification()
                        }
                        
                    }
                } catch (e: Exception) {
                    android.util.Log.e("BlockingVpnService", "Packet processing loop failed: ${e.message}", e)
                } finally {
                    android.util.Log.i("BlockingVpnService", "Packet processing stopped. Stats - Processed: $packetsProcessed, Blocked: $packetsBlocked")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("BlockingVpnService", "Packet processing loop failed: ${e.message}", e)
        } finally {
            android.util.Log.i("BlockingVpnService", "Packet processing stopped. Stats - Processed: $packetsProcessed, Blocked: $packetsBlocked")
        }
    }
    
    private suspend fun analyzePacket(packet: com.alhaq.amnshield.guardian.network.IpPacket): Boolean {
        // Process packet through content filter
        contentFilter.processPacket(packet)
        
        // Check DNS queries
        val dnsQuery = packetParser.parseDnsQuery(packet)
        if (dnsQuery != null) {
            val result = contentFilter.shouldBlockDns(dnsQuery)
            if (result.shouldBlock) {
                return true
            }
        }
        
        // Check HTTP requests
        val httpRequest = packetParser.parseHttpRequest(packet)
        if (httpRequest != null) {
            val result = contentFilter.shouldBlockHttp(httpRequest)
            if (result.shouldBlock) {
                return true
            }
        }
        
        // Legacy domain blocking for compatibility
        if (shouldBlockDomain(packet.destinationAddress)) {
            return true
        }
        
        return false
    }
    
    // Legacy method for compatibility
    internal fun shouldBlockDomain(host: String): Boolean {
        val normalized = BlockUtils.normalizeDomain(host)
        return BlockUtils.matchDomain(blockedDomains, normalized)
    }

    // For unit tests and debug tools
    @androidx.annotation.VisibleForTesting
    fun setBlockedDomainsForTesting(domains: Set<String>) {
        blockedDomains = domains
        contentFilter.updateConfiguration(domains = domains)
    }
    
    private fun updateConfiguration(intent: Intent) {
        val domains = intent.getStringArrayExtra(EXTRA_BLOCKED_DOMAINS)
        val blockSocialMedia = intent.getBooleanExtra(EXTRA_BLOCK_SOCIAL_MEDIA, false)
        val blockAdultContent = intent.getBooleanExtra(EXTRA_BLOCK_ADULT_CONTENT, true)
        val blockKeywords = intent.getBooleanExtra(EXTRA_BLOCK_KEYWORDS, false)
        
        // Merge custom domains with default harmful websites
        val mergedDomains = if (domains != null) {
            (domains.toSet() + com.alhaq.amnshield.guardian.util.BlockLists.harmfulWebsites)
        } else {
            com.alhaq.amnshield.guardian.util.BlockLists.harmfulWebsites
        }
        
        blockedDomains = mergedDomains
        
        contentFilter.updateConfiguration(
            domains = mergedDomains,
            keywords = if (blockKeywords) com.alhaq.amnshield.guardian.util.BlockLists.harmfulKeywords else emptySet(),
            socialMedia = blockSocialMedia,
            adultContent = blockAdultContent,
            gambling = false
        )
        
        android.util.Log.i("BlockingVpnService", 
            "Configuration updated: ${mergedDomains.size} domains, social=$blockSocialMedia, keywords=$blockKeywords")
    }
    
    private fun setupNotificationObserver() {
        serviceScope.launch {
            contentFilter.blockEvents.collect { event ->
                // Handle block events for notifications/logging
                sendBlockNotification(event)
            }
        }
    }
    
    private fun sendBlockNotification(event: com.alhaq.amnshield.guardian.network.BlockEvent) {
        // Create notification for blocked content
        val notification = NotificationCompat.Builder(this, BlockerApplication.BLOCKING_CHANNEL_ID)
            .setContentTitle("Content Blocked")
            .setContentText("${event.category}: ${event.domain}")
            .setSmallIcon(R.drawable.ic_block)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(event.timestamp.toInt(), notification)
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, BlockerApplication.SERVICE_CHANNEL_ID)
            .setContentTitle("AmnShield Protection Active")
            .setContentText("Filtering network traffic")
            .setSmallIcon(R.drawable.ic_shield)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private fun updateNotification() {
        val notification = NotificationCompat.Builder(this, BlockerApplication.SERVICE_CHANNEL_ID)
            .setContentTitle("AmnShield Protection Active")
            .setContentText("Processed: $packetsProcessed | Blocked: $packetsBlocked")
            .setSmallIcon(R.drawable.ic_shield)
            .setOngoing(true)
            .build()
        
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildIpv4UdpResponse(request: com.alhaq.amnshield.guardian.network.IpPacket, udpPayload: ByteArray): ByteArray? {
        // Minimal IPv4 + UDP response builder; not fully RFC-compliant but adequate for MVP testing.
        try {
            val ipHeaderLen = 20
            val udpHeaderLen = 8
            val totalLen = ipHeaderLen + udpHeaderLen + udpPayload.size
            val buffer = ByteBuffer.allocate(totalLen)

            // IPv4 header
            buffer.put(((4 shl 4) or 5).toByte()) // Version=4, IHL=5
            buffer.put(0x00) // DSCP/ECN
            buffer.putShort(totalLen.toShort())
            buffer.putShort(0) // Identification
            buffer.putShort(0x4000.toShort()) // Flags/Fragment (DF)
            buffer.put(64) // TTL
            buffer.put(PacketParser.PROTOCOL_UDP.toByte())
            buffer.putShort(0) // checksum placeholder
            val src = InetAddress.getByName(request.destinationAddress).address
            val dst = InetAddress.getByName(request.sourceAddress).address
            buffer.put(src)
            buffer.put(dst)

            // Compute IPv4 header checksum
            buffer.rewind()
            var sum = 0L
            repeat(10) { i ->
                val word = if (i == 5) 0 else buffer.short.toInt() and 0xFFFF
                sum += word
            }
            while ((sum shr 16) != 0L) sum = (sum and 0xFFFF) + (sum shr 16)
            val ipChecksum = (sum.inv().toInt() and 0xFFFF).toShort()
            buffer.position(10)
            buffer.putShort(ipChecksum)

            // UDP header
            buffer.position(ipHeaderLen)
            buffer.putShort(request.destinationPort.toShort()) // src port = original dst port
            buffer.putShort(request.sourcePort.toShort()) // dst port = original src port
            buffer.putShort((udpHeaderLen + udpPayload.size).toShort())
            buffer.putShort(0) // checksum (optional for IPv4)
            buffer.put(udpPayload)

            return buffer.array()
        } catch (e: Exception) {
            android.util.Log.w("BlockingVpnService", "Failed to build UDP response", e)
            return null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpnService()
        serviceScope.cancel()
    }
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        
        const val ACTION_START = "com.alhaq.amnshield.guardian.vpn.START"
        const val ACTION_STOP = "com.alhaq.amnshield.guardian.vpn.STOP"
        const val ACTION_UPDATE_CONFIG = "com.alhaq.amnshield.guardian.vpn.UPDATE_CONFIG"
        
        const val EXTRA_BLOCKED_DOMAINS = "blocked_domains"
        const val EXTRA_BLOCK_SOCIAL_MEDIA = "block_social_media"
        const val EXTRA_BLOCK_ADULT_CONTENT = "block_adult_content"
        const val EXTRA_BLOCK_GAMBLING = "block_gambling"
        const val EXTRA_BLOCK_KEYWORDS = "block_keywords"
    }
}
