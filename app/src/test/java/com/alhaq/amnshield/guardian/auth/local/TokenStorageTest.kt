package com.alhaq.amnshield.guardian.auth.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alhaq.amnshield.guardian.auth.model.CapabilityToken
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit tests for TokenStorage
 * 
 * Tests token encryption, storage, expiration, and revocation.
 */
@RunWith(AndroidJUnit4::class)
class TokenStorageTest {
    
    private lateinit var context: Context
    private lateinit var tokenStorage: TokenStorage
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        tokenStorage = TokenStorage.getInstance(context)
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
            grantedBy = "com.alhaq.amnshield.guardian",
            grantedTo = "com.alhaq.deenshield",
            capabilities = """["MANAGE_APP_BLOCKING"]""",
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + (90 * 24 * 60 * 60 * 1000L),
            encryptedData = byteArrayOf(1, 2, 3, 4),
            hmacSignature = "test-signature"
        )
        
        val storeResult = tokenStorage.storeToken(token)
        assertTrue(storeResult.success)
        
        val retrievedResult = tokenStorage.retrieveToken("test-session-123")
        assertTrue(retrievedResult.success)
        assertNotNull(retrievedResult.token)
        assertEquals(token.sessionId, retrievedResult.token!!.sessionId)
        assertEquals(token.grantedTo, retrievedResult.token!!.grantedTo)
        assertEquals(token.grantedBy, retrievedResult.token!!.grantedBy)
    }
    
    @Test
    fun testTokenExpiration_ExpiredToken() {
        val expiredToken = CapabilityToken(
            sessionId = "expired-session",
            grantedBy = "com.alhaq.amnshield.guardian",
            grantedTo = "com.alhaq.deenshield",
            capabilities = """["MANAGE_APP_BLOCKING"]""",
            createdAt = System.currentTimeMillis() - (100 * 24 * 60 * 60 * 1000L), // 100 days ago
            expiresAt = System.currentTimeMillis() - (10 * 24 * 60 * 60 * 1000L), // Expired 10 days ago
            encryptedData = byteArrayOf(1, 2, 3, 4),
            hmacSignature = "test-signature"
        )
        
        assertTrue(expiredToken.isExpired())
        assertFalse(expiredToken.isValid())
        
        tokenStorage.storeToken(expiredToken)
        
        val retrieveResult = tokenStorage.retrieveToken("expired-session")
        assertFalse(retrieveResult.success)
        assertTrue(retrieveResult.error?.contains("expired", ignoreCase = true) == true)
    }
    
    @Test
    fun testTokenExpiration_ValidToken() {
        val validToken = CapabilityToken(
            sessionId = "valid-session",
            grantedBy = "com.alhaq.amnshield.guardian",
            grantedTo = "com.alhaq.deenshield",
            capabilities = """["MANAGE_APP_BLOCKING"]""",
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + (90 * 24 * 60 * 60 * 1000L), // 90 days from now
            encryptedData = byteArrayOf(1, 2, 3, 4),
            hmacSignature = "test-signature"
        )
        
        assertFalse(validToken.isExpired())
        assertTrue(validToken.isValid())
        
        tokenStorage.storeToken(validToken)
        
        val retrieveResult = tokenStorage.retrieveToken("valid-session")
        assertTrue(retrieveResult.success)
        assertNotNull(retrieveResult.token)
    }
    
    @Test
    fun testTokenRevocation() {
        val token = CapabilityToken(
            sessionId = "revoke-session",
            grantedBy = "com.alhaq.amnshield.guardian",
            grantedTo = "com.alhaq.deenshield",
            capabilities = """["MANAGE_APP_BLOCKING"]""",
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + (90 * 24 * 60 * 60 * 1000L),
            encryptedData = byteArrayOf(1, 2, 3, 4),
            hmacSignature = "test-signature"
        )
        
        tokenStorage.storeToken(token)
        
        // Token should be valid initially
        val initialRetrieve = tokenStorage.retrieveToken("revoke-session")
        assertTrue(initialRetrieve.success)
        
        // Revoke token
        val revokeResult = tokenStorage.revokeToken("revoke-session")
        assertTrue(revokeResult.success)
        
        // Token should no longer be retrieved successfully
        val postRevokeRetrieve = tokenStorage.retrieveToken("revoke-session")
        assertFalse(postRevokeRetrieve.success)
        assertTrue(postRevokeRetrieve.error?.contains("revoked", ignoreCase = true) == true)
    }
    
    @Test
    fun testRetrieveToken_NonExistent() {
        val retrieveResult = tokenStorage.retrieveToken("non-existent-session")
        assertFalse(retrieveResult.success)
        assertNull(retrieveResult.token)
    }
    
    @Test
    fun testClearAllTokens() {
        // Store multiple tokens
        for (i in 1..5) {
            val token = CapabilityToken(
                sessionId = "session-$i",
                grantedBy = "com.alhaq.amnshield.guardian",
                grantedTo = "com.alhaq.deenshield",
                capabilities = """["MANAGE_APP_BLOCKING"]""",
                createdAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + (90 * 24 * 60 * 60 * 1000L),
                encryptedData = byteArrayOf(1, 2, 3, 4),
                hmacSignature = "test-signature-$i"
            )
            tokenStorage.storeToken(token)
        }
        
        // Clear all tokens
        tokenStorage.clearAllTokens()
        
        // Verify all tokens are gone
        for (i in 1..5) {
            val retrieveResult = tokenStorage.retrieveToken("session-$i")
            assertFalse(retrieveResult.success)
            assertNull(retrieveResult.token)
        }
    }
}
