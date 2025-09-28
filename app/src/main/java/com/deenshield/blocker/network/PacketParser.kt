package com.deenshield.blocker.network

import kotlinx.serialization.Serializable
import java.net.InetAddress
import java.nio.ByteBuffer

/**
 * Represents an IP packet for analysis
 */
@Serializable
data class IpPacket(
    val version: Int,
    val protocol: Int,
    val sourceAddress: String,
    val destinationAddress: String,
    val sourcePort: Int = 0,
    val destinationPort: Int = 0,
    val payload: ByteArray = byteArrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as IpPacket
        return version == other.version &&
                protocol == other.protocol &&
                sourceAddress == other.sourceAddress &&
                destinationAddress == other.destinationAddress &&
                sourcePort == other.sourcePort &&
                destinationPort == other.destinationPort &&
                payload.contentEquals(other.payload)
    }

    override fun hashCode(): Int {
        var result = version
        result = 31 * result + protocol
        result = 31 * result + sourceAddress.hashCode()
        result = 31 * result + destinationAddress.hashCode()
        result = 31 * result + sourcePort
        result = 31 * result + destinationPort
        result = 31 * result + payload.contentHashCode()
        return result
    }
}

/**
 * DNS query information
 */
@Serializable
data class DnsQuery(
    val domain: String,
    val queryType: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * HTTP request information
 */
@Serializable
data class HttpRequest(
    val method: String,
    val url: String,
    val host: String,
    val userAgent: String = "",
    val headers: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Network packet parser for VPN traffic analysis
 */
class PacketParser {
    
    companion object {
        const val PROTOCOL_TCP = 6
        const val PROTOCOL_UDP = 17
        const val DNS_PORT = 53
        const val HTTP_PORT = 80
        const val HTTPS_PORT = 443
    }

    /**
     * Parse raw IP packet from VPN interface
     */
    fun parseIpPacket(buffer: ByteBuffer): IpPacket? {
        if (buffer.remaining() < 20) return null // Minimum IP header size
        
        val version = (buffer.get(0).toInt() and 0xF0) shr 4
        val protocol = buffer.get(9).toInt() and 0xFF
        
        val sourceBytes = ByteArray(4)
        val destBytes = ByteArray(4)
        
        buffer.position(12)
        buffer.get(sourceBytes)
        buffer.get(destBytes)
        
        val sourceAddress = InetAddress.getByAddress(sourceBytes).hostAddress ?: ""
        val destinationAddress = InetAddress.getByAddress(destBytes).hostAddress ?: ""
        
        var sourcePort = 0
        var destinationPort = 0
        val payload = ByteArray(buffer.remaining() - 20)
        
        if (protocol == PROTOCOL_TCP || protocol == PROTOCOL_UDP) {
            if (buffer.remaining() >= 24) { // IP header + TCP/UDP header
                buffer.position(20)
                sourcePort = buffer.short.toInt() and 0xFFFF
                destinationPort = buffer.short.toInt() and 0xFFFF
                
                val payloadStart = if (protocol == PROTOCOL_TCP) 40 else 28 // TCP header is larger
                if (buffer.remaining() >= payloadStart - 20) {
                    buffer.position(payloadStart)
                    if (buffer.remaining() > 0) {
                        val payloadSize = minOf(payload.size, buffer.remaining())
                        buffer.get(payload, 0, payloadSize)
                    }
                }
            }
        }
        
        return IpPacket(
            version = version,
            protocol = protocol,
            sourceAddress = sourceAddress,
            destinationAddress = destinationAddress,
            sourcePort = sourcePort,
            destinationPort = destinationPort,
            payload = payload
        )
    }
    
    /**
     * Extract DNS query from UDP packet
     */
    fun parseDnsQuery(packet: IpPacket): DnsQuery? {
        if (packet.protocol != PROTOCOL_UDP || packet.destinationPort != DNS_PORT) return null
        if (packet.payload.size < 12) return null // Minimum DNS header size
        
        try {
            val domain = extractDomainFromDnsQuery(packet.payload)
            return DnsQuery(
                domain = domain,
                queryType = "A" // Simplified - could parse actual query type
            )
        } catch (e: Exception) {
            return null
        }
    }
    
    /**
     * Extract HTTP request from TCP packet
     */
    fun parseHttpRequest(packet: IpPacket): HttpRequest? {
        if (packet.protocol != PROTOCOL_TCP || packet.destinationPort != HTTP_PORT) return null
        if (packet.payload.isEmpty()) return null
        
        try {
            val payloadString = String(packet.payload, Charsets.UTF_8)
            val lines = payloadString.split("\r\n")
            if (lines.isEmpty()) return null
            
            val requestLine = lines[0]
            val parts = requestLine.split(" ")
            if (parts.size < 3) return null
            
            val method = parts[0]
            val path = parts[1]
            
            var host = ""
            var userAgent = ""
            val headers = mutableMapOf<String, String>()
            
            for (i in 1 until lines.size) {
                val line = lines[i]
                if (line.isBlank()) break
                
                val colonIndex = line.indexOf(':')
                if (colonIndex > 0) {
                    val headerName = line.substring(0, colonIndex).trim()
                    val headerValue = line.substring(colonIndex + 1).trim()
                    headers[headerName] = headerValue
                    
                    when (headerName.lowercase()) {
                        "host" -> host = headerValue
                        "user-agent" -> userAgent = headerValue
                    }
                }
            }
            
            val url = if (host.isNotEmpty()) "http://$host$path" else path
            
            return HttpRequest(
                method = method,
                url = url,
                host = host,
                userAgent = userAgent,
                headers = headers.toMap()
            )
        } catch (e: Exception) {
            return null
        }
    }
    
    private fun extractDomainFromDnsQuery(payload: ByteArray): String {
        if (payload.size < 12) return ""
        
        var position = 12 // Skip DNS header
        val domain = StringBuilder()
        
        while (position < payload.size) {
            val length = payload[position].toInt() and 0xFF
            if (length == 0) break
            
            position++
            if (position + length > payload.size) break
            
            if (domain.isNotEmpty()) domain.append('.')
            domain.append(String(payload, position, length, Charsets.UTF_8))
            position += length
        }
        
        return domain.toString().lowercase()
    }
}