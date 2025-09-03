package com.deenshield.blocker

import com.deenshield.blocker.service.BlockingVpnService
import com.deenshield.blocker.service.AccessibilityBlocker
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceLogicTest {
    @Test
    fun vpnBlocks_TestDomains() {
        val svc = BlockingVpnService()
        svc.blockedDomains = setOf("example-porn.com", "badexample.com")
        assertTrue(svc.shouldBlockDomain("https://example-porn.com/page"))
        assertTrue(svc.shouldBlockDomain("sub.badexample.com"))
        assertFalse(svc.shouldBlockDomain("good.com"))
    }

    @Test
    fun accessibility_RedirectsBlockedApp() {
        val acc = AccessibilityBlocker()
        acc.blockedApps = setOf("com.instagram.android", "com.tiktok.android")
        assertTrue(acc.shouldRedirectForPackage("com.instagram.android"))
        assertFalse(acc.shouldRedirectForPackage("com.safe.app"))
    }
}
