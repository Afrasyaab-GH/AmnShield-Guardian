package org.alhaq.deenshield.guardian

import org.alhaq.deenshield.guardian.util.BlockUtils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockUtilsTest {
    @Test
    fun keywordMatching_Works() {
        val kws = setOf("adult", "casino", "xxx")
        assertTrue(BlockUtils.matchKeywords(kws, "This page contains Adult content"))
        assertTrue(BlockUtils.matchKeywords(kws, "Visit Best Casino Sites"))
        assertFalse(BlockUtils.matchKeywords(kws, "Safe educational content"))
    }

    @Test
    fun domainMatching_WorksWithSubdomains() {
        val blocked = setOf("example.com")
        assertTrue(BlockUtils.matchDomain(blocked, "example.com"))
        assertTrue(BlockUtils.matchDomain(blocked, "sub.example.com"))
        assertFalse(BlockUtils.matchDomain(blocked, "another.com"))
    }
}
