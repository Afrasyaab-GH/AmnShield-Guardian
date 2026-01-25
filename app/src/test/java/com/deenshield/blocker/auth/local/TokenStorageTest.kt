package com.deenshield.blocker.auth.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.deenshield.blocker.auth.CapabilityToken
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit tests for TokenStorage
 * 
 * Tests token encryption, storage, expiration, and revocation.
 * 
 * **Coverage Areas:**
 * - AES-256 encryption/decryption
 * - HMAC signature verification
 * - Token expiration logic
 * - Revocation tracking
 * - Edge cases (null tokens, corrupted data)
 */
@RunWith(AndroidJUnit4::class)
class TokenStorageTest {
    
    private lateinit var context: Context
    private lateinit var tokenStorage: TokenStorage
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        tokenStorage = TokenStorage.getInstance(context)
        
        // Clear any existing tokens
        tokenStorage.clearAllTokens()
    }
    
    @After
    fun tearDown() {
        tokenStorage.clearAllTokens()
    }
    
    @Test
    fun testTokenStorage_StoreAndRetrieve() {
        val token = CapabilityToken(
            sessionId = "test-session-123",
            grantedTo = "com.alhaq.deenshield",
            grantedBy = "com.deenshield.blocker",
            capabilities = setOf("MANAGE_APP_BLOCKING"),
            encryptedData = byteArrayOf(1, 2, 3, 4),
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + (90 * 24 * 60 * 60 * 1000L),
            signature = "test-signature"
        )
        
        val storeResult = tokenStorage.storeToken(token)
        assertTrue(storeResult)
        
        val retrievedToken = tokenStorage.getToken("test-session-123")
        assertNotNull(retrievedToken)
        assertEquals(token.sessionId, retrievedToken!!.sessionId)
        assertEquals(token.grantedTo, retrievedToken.grantedTo)
    }
    
    @Test
    fun testTokenExpiration_ExpiredToken() {
        val expiredToken = CapabilityToken(
            sessionId = "expired-session",
            grantedTo = "com.alhaq.deenshield",
            grantedBy = "com.deenshield.blocker",
            capabilities = setOf("MANAGE_APP_BLOCKING"),
            encryptedData = byteArrayOf(1, 2, 3, 4),
            createdAt = System.currentTimeMillis() - (100 * 24 * 60 * 60 * 1000L), // 100 days ago
            expiresAt = System.currentTimeMillis() - (10 * 24 * 60 * 60 * 1000L), // Expired 10 days ago
            signature = "test-signature"
        )
        
        tokenStorage.storeToken(expiredToken)
        
        val isExpired = tokenStorage.isTokenExpired(expiredToken)
        assertTrue(isExpired)
        
        val isValid = tokenStorage.isTokenValid(expiredToken)
        assertFalse(isValid)
    }
    
    @Test
    fun testTokenExpiration_ValidToken() {
        val validToken = CapabilityToken(
            sessionId = "valid-session",
            grantedTo = "com.alhaq.deenshield",
            grantedBy = "com.deenshield.blocker",
            capabilities = setOf("MANAGE_APP_BLOCKING"),
            encryptedData = byteArrayOf(1, 2, 3, 4),
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + (90 * 24 * 60 * 60 * 1000L), // 90 days from now
            signature = "test-signature"
        )
        
        tokenStorage.storeToken(validToken)
        
        val isExpired = tokenStorage.isTokenExpired(validToken)
        assertFalse(isExpired)
        
        val isValid = tokenStorage.isTokenValid(validToken)
        assertTrue(isValid)
    }
    
    @Test
    fun testTokenRevocation() {
        val token = CapabilityToken(
            sessionId = "revoke-session",
            grantedTo = "com.alhaq.deenshield",
            grantedBy = "com.deenshield.blocker",
            capabilities = setOf("MANAGE_APP_BLOCKING"),
            encryptedData = byteArrayOf(1, 2, 3, 4),
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + (90 * 24 * 60 * 60 * 1000L),
            signature = "test-signature"
        )
        
        tokenStorage.storeToken(token)
        
        // Token should be valid before revocation
        assertTrue(tokenStorage.isTokenValid(token))
        
        // Revoke token
        tokenStorage.revokeToken("revoke-session")
        
        // Token should no longer be valid
        val revokedToken = tokenStorage.getToken("revoke-session")
        assertNotNull(revokedToken)
        assertFalse(tokenStorage.isTokenValid(revokedToken!!))
    }
    
    @Test
    fun testGetToken_NonExistent() {
        val token = tokenStorage.getToken("non-existent-session")
        assertNull(token)
    }
    
    @Test
    fun testClearAllTokens() {
        // Store multiple tokens
        for (i in 1..5) {
            val token = CapabilityToken(
                sessionId = "session-$i",
                grantedTo = "com.alhaq.deenshield",
                grantedBy = "com.deenshield.blocker",
                capabilities = setOf("MANAGE_APP_BLOCKING"),
                encryptedData = byteArrayOf(1, 2, 3, 4),
                createdAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + (90 * 24 * 60 * 60 * 1000L),
                signature = "test-signature-$i"
            )
            tokenStorage.storeToken(token)
        }
        
        // Clear all tokens
        tokenStorage.clearAllTokens()
        
        // Verify all tokens are gone
        for (i in 1..5) {
            val token = tokenStorage.getToken("session-$i")
            assertNull(token)
        }
    }
    
    @Test
    fun testEncryptedStorage_DataNotPlaintext() {
        val token = CapabilityToken(
            sessionId = "encrypted-session",
            grantedTo = "com.alhaq.deenshield",
            grantedBy = "com.deenshield.blocker",
            capabilities = setOf("MANAGE_APP_BLOCKING", "MANAGE_KEYWORDS"),
            encryptedData = "SECRET_DATA".toByteArray(),
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + (90 * 24 * 60 * 60 * 1000L),
            signature = "test-signature"
        )
        
        tokenStorage.storeToken(token)
        
        // Access underlying SharedPreferences directly
        val prefs = context.getSharedPreferences("token_storage", Context.MODE_PRIVATE)
        val rawData = prefs.getString("token_encrypted-session", null)
        
        // Raw data should NOT contain plaintext capabilities
        assertNotNull(rawData)
        assertFalse(rawData!!.contains("MANAGE_APP_BLOCKING"))
        assertFalse(rawData.contains("MANAGE_KEYWORDS"))
        assertFalse(rawData.contains("SECRET_DATA"))
    }
    
    @Test
    fun testSignatureVerification_ValidSignature() {
        val token = CapabilityToken(
            sessionId = "sig-session",
            grantedTo = "com.alhaq.deenshield",
            grantedBy = "com.deenshield.blocker",
            capabilities = setOf("MANAGE_APP_BLOCKING"),
            encryptedData = byteArrayOf(1, 2, 3, 4),
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + (90 * 24 * 60 * 60 * 1000L),
            signature = tokenStorage.generateSignature("test-data")
        )
        
        val isValid = tokenStorage.verifySignature(token, "test-data")
        assertTrue(isValid)
    }
    
    @Test
    fun testSignatureVerification_InvalidSignature() {
        val token = CapabilityToken(
            sessionId = "sig-session",
            grantedTo = "com.alhaq.deenshield",
            grantedBy = "com.deenshield.blocker",
            capabilities = setOf("MANAGE_APP_BLOCKING"),
            encryptedData = byteArrayOf(1, 2, 3, 4),
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + (90 * 24 * 60 * 60 * 1000L),
            signature = "invalid-signature"
        )
        
        val isValid = tokenStorage.verifySignature(token, "test-data")
        assertFalse(isValid)
    }
}
