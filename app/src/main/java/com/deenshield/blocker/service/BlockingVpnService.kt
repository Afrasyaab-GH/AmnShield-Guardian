package com.deenshield.blocker.service

import android.net.VpnService
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Local-only VPNService placeholder. Real packet parsing is complex; this scaffolds
 * a tun interface and a loop where DNS/HTTP inspection can be added later.
 * No external network calls, on-device only.
 */
class BlockingVpnService : VpnService() {
    private var tun: ParcelFileDescriptor? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    // Predefined block lists (populate from repository or preferences)
    @Volatile var blockedDomains: Set<String> = emptySet()

    internal fun shouldBlockDomain(host: String): Boolean {
        // Simple check; real path would parse DNS queries or TLS SNI and pass here
        val normalized = com.deenshield.blocker.util.BlockUtils.normalizeDomain(host)
        return com.deenshield.blocker.util.BlockUtils.matchDomain(blockedDomains, normalized)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        // Build TUN interface
        if (tun == null) {
            tun = Builder()
                .addAddress("10.0.0.2", 24)
                .setSession("DeenShield Local VPN")
                .establish()
        }
        if (job == null) {
            job = scope.launch { runLoop() }
        }
        return START_STICKY
    }

    private suspend fun runLoop() {
        // NOTE: This is a stub; real implementation would parse IP packets from tun!!.fileDescriptor
        // For now, this just idles. Hook your DNS matching (DoH inside app if desired) or SNI parsing for TLS.
        while (tun != null) {
            kotlinx.coroutines.delay(1000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel(); job = null
        tun?.close(); tun = null
    }
}
