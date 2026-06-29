package com.alhaq.amnshield.guardian.util

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

    /**
     * Validate domain format before adding to block list
     * @return null if valid, error message if invalid
     */
    fun validateDomain(domain: String): String? {
        val trimmed = domain.trim()
        if (trimmed.isEmpty()) return "Domain cannot be empty"
        if (trimmed.length > 255) return "Domain too long (max 255 characters)"
        
        // Remove protocol if present
        var d = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed.substringAfter("://")
        } else {
            trimmed
        }
        
        // Remove path if present
        d = d.substringBefore('/')
        d = d.substringBefore(':')
        d = d.lowercase()
        
        if (d.isEmpty()) return "Invalid domain format"
        
        // Valid domain characters
        if (!d.all { it.isLetterOrDigit() || it == '.' || it == '-' }) {
            return "Domain contains invalid characters"
        }
        
        // Must have at least one dot for TLD
        if (!d.contains('.')) return "Domain must include a TLD (e.g., example.com)"
        
        val parts = d.split('.')
        if (parts.any { it.isEmpty() }) return "Domain has empty parts"
        
        return null // Valid
    }

    /**
     * Validate keyword before adding to block list
     * @return null if valid, error message if invalid
     */
    fun validateKeyword(keyword: String): String? {
        val trimmed = keyword.trim()
        if (trimmed.isEmpty()) return "Keyword cannot be empty"
        if (trimmed.length > 100) return "Keyword too long (max 100 characters)"
        if (trimmed.length < 3) return "Keyword must be at least 3 characters"
        return null // Valid
    }

    /**
     * Validate and clean a list of domains
     * @return Pair of (valid domains, error messages)
     */
    fun validateDomainList(csvDomains: String): Pair<Set<String>, List<String>> {
        val errors = mutableListOf<String>()
        val valid = mutableSetOf<String>()
        
        csvDomains.split(',').forEach { domain ->
            val error = validateDomain(domain)
            if (error != null) {
                errors.add("'${domain.trim()}': $error")
            } else {
                valid.add(normalizeDomain(domain))
            }
        }
        
        return Pair(valid, errors)
    }

    /**
     * Validate and clean a list of keywords
     * @return Pair of (valid keywords, error messages)
     */
    fun validateKeywordList(csvKeywords: String): Pair<Set<String>, List<String>> {
        val errors = mutableListOf<String>()
        val valid = mutableSetOf<String>()
        
        csvKeywords.split(',').forEach { keyword ->
            val error = validateKeyword(keyword)
            if (error != null) {
                errors.add("'${keyword.trim()}': $error")
            } else {
                valid.add(keyword.trim().lowercase())
            }
        }
        
        return Pair(valid, errors)
    }
}

