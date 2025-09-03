package com.deenshield.blocker.util

object BlockUtils {
    // Simple domain normalization
    fun normalizeDomain(input: String): String {
        var s = input.trim().lowercase()
        if (s.startsWith("http://") || s.startsWith("https://")) s = s.substringAfter("://")
        if (s.startsWith("www.")) s = s.removePrefix("www.")
        s = s.substringBefore('/')
        s = s.substringBefore(':')
        return s.filter { it.isLetterOrDigit() || it == '.' || it == '-' }
    }

    fun matchDomain(blocked: Set<String>, host: String): Boolean {
        val h = normalizeDomain(host)
        if (h.isEmpty()) return false
        if (blocked.contains(h)) return true
        // Subdomain match
        val parts = h.split('.')
        for (i in 1 until parts.size - 1) {
            val suffix = parts.drop(i).joinToString(".")
            if (blocked.contains(suffix)) return true
        }
        return false
    }

    fun matchKeywords(blocked: Set<String>, text: String): Boolean {
        if (blocked.isEmpty()) return false
        val t = text.lowercase()
        return blocked.any { kw -> kw.length >= 3 && t.contains(kw) }
    }
}
