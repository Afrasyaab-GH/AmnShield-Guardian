package com.deenshield.blocker.network

import android.util.Log
import com.deenshield.blocker.util.BlockUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer

/**
 * Very small DNS proxy helper to craft simple responses or forward queries.
 * NOTE: This is deliberately minimal for on-device filtering; production-ready
 * DNS proxies require robust parsing and full RFC compliance.
 */
class DnsProxy(
    private val upstreamHosts: List<String> = listOf("1.1.1.1", "8.8.8.8"),
    private val upstreamPort: Int = 53
) {
    suspend fun processDnsQuery(query: ByteArray, blockedDomains: Set<String>): ByteArray? = withContext(Dispatchers.IO) {
        return@withContext try {
            val domain = extractDomainFromQuery(query)
            if (domain.isNotBlank() && BlockUtils.matchDomain(blockedDomains, domain)) {
                // Craft NXDOMAIN response
                craftNxDomainResponse(query)
            } else {
                // Forward to upstream and return response
                forwardToUpstream(query)
            }
        } catch (e: Exception) {
            Log.w("DnsProxy", "Failed to handle DNS query", e)
            null
        }
    }

    private fun extractDomainFromQuery(payload: ByteArray): String {
        if (payload.size < 12) return ""
        var pos = 12
        val sb = StringBuilder()
        while (pos < payload.size) {
            val len = payload[pos].toInt() and 0xFF
            if (len == 0) break
            pos += 1
            if (pos + len > payload.size) break
            if (sb.isNotEmpty()) sb.append('.')
            sb.append(String(payload, pos, len, Charsets.UTF_8))
            pos += len
        }
        return sb.toString().lowercase()
    }

    private fun craftNxDomainResponse(query: ByteArray): ByteArray {
        val buf = ByteBuffer.wrap(query)
        val txId = buf.short // transaction id
        // Build header
        val out = ByteArrayOutputStream()
        out.write(query[0].toInt())
        out.write(query[1].toInt())
        // Flags: response, authoritative, rcode=3 (NXDOMAIN)
        out.write(0x81) // QR=1, OPCODE=0, AA=0, TC=0, RD=1
        out.write(0x83) // RA=1, Z=0, RCODE=3
        // QDCOUNT
        out.write(query[4].toInt())
        out.write(query[5].toInt())
        // ANCOUNT, NSCOUNT, ARCOUNT = 0
        out.write(0x00); out.write(0x00)
        out.write(0x00); out.write(0x00)
        out.write(0x00); out.write(0x00)
        // Copy question section
        val qdCount = ((query[4].toInt() and 0xFF) shl 8) or (query[5].toInt() and 0xFF)
        var pos = 12
        repeat(qdCount) {
            while (pos < query.size) {
                val len = query[pos].toInt() and 0xFF
                pos += 1
                if (len == 0) break
                pos += len
            }
            // zero length label
            // QTYPE,QCLASS
            pos += 1 + 2 + 2
        }
        out.write(query, 12, pos - 12)
        return out.toByteArray()
    }

    private fun forwardToUpstream(query: ByteArray): ByteArray? {
        var lastEx: Exception? = null
        upstreamHosts.forEach { host ->
            try {
                DatagramSocket().use { sock ->
                    sock.soTimeout = 2000
                    val addr = InetAddress.getByName(host)
                    val req = DatagramPacket(query, query.size, addr, upstreamPort)
                    sock.send(req)
                    val respBuf = ByteArray(2048)
                    val resp = DatagramPacket(respBuf, respBuf.size)
                    sock.receive(resp)
                    return resp.data.copyOf(resp.length)
                }
            } catch (e: Exception) {
                lastEx = e
            }
        }
        if (lastEx != null) Log.w("DnsProxy", "All upstreams failed", lastEx)
        return null
    }
}
